/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.bhl.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.JsonNode;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemStatus;

/**
 * Service object that facades the process of interacting with the SOLR index
 * 
 * @author baird
 * 
 */
public class IndexingService extends AbstractService {

	private final String _serverURL;
	private DocumentCacheService _docCache;

	private static Pattern PAGE_FILE_REGEX = Pattern.compile("^(\\d{5})_(\\d+).txt$");
	private static Pattern SINGLE_YEAR_PATTERN = Pattern.compile("(\\d{4})");
	private static Pattern YEAR_RANGE_PATTERN = Pattern.compile("(\\d{4})\\s*[^\\d]\\s*(\\d{4})");
	private static Pattern ABBREV_RANGE_PATTERN = Pattern.compile("(\\d{4})\\s*[^\\d]\\s*(\\d{2})");

	/**
	 * CTOR
	 * 
	 * @param serverUrl
	 * @param docCache
	 */
	public IndexingService(String serverUrl, DocumentCacheService docCache) {
		_serverURL = serverUrl;
		_docCache = docCache;
	}

	/**
	 * Create a new instance of the Solr server API facade
	 * 
	 * @return
	 */
	private SolrServer createSolrServer() {
		try {
			return new CommonsHttpSolrServer(_serverURL);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Indexes an item that exists in the document cache
	 * 
	 * @param item
	 */
	public void indexItem(final ItemDescriptor item) {

		String itemPathStr = _docCache.getItemDirectoryPath(item.getInternetArchiveId());

		SolrServer server = createSolrServer();

		log("Indexing pages %s for item %s", itemPathStr, item.getItemId());

		try {
			File itemPath = new File(itemPathStr);
			if (itemPath.exists() && itemPath.isDirectory()) {
				File[] pageFiles = itemPath.listFiles();
				int pageCount = 0;
				for (File pageFile : pageFiles) {
					Matcher m = PAGE_FILE_REGEX.matcher(pageFile.getName());
					if (m.matches()) {
						String pageId = m.group(2);
						String pageText = FileUtils.readFileToString(pageFile);
						indexPage(item, pageId, pageText, server);
						pageCount++;

						if (pageCount % 100 == 0) {
							server.commit();
						}
					}
				}
				if (pageCount > 0) {
					server.commit();
					getItemsService().setItemStatus(item.getItemId(), ItemStatus.INDEXED, pageCount);
					log("%d pages indexed for item: %s", pageCount, item.getItemId());
				} else {
					log("Ignoring empty item (no pages): %s", item.getItemId());
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Send a page of text to SOLR for indexing
	 * 
	 * @param item
	 * @param pageId
	 * @param pageText
	 * @param server
	 */
	private void indexPage(ItemDescriptor item, String pageId, String pageText, SolrServer server) {
		if (!StringUtils.isEmpty(pageText)) {
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", pageId, 1.0f);
			doc.addField("name", item.getTitle(), 1.0f);
			doc.addField("text", pageText);
			doc.addField("internetArchiveId", item.getInternetArchiveId());
			doc.addField("itemId", item.getItemId());
			doc.addField("pageId", pageId, 1.0f);
			doc.addField("pageUrl", String.format("http://bhl.ala.org.au/pageimage/%s", pageId));

			JsonNode metadata = _docCache.getItemMetaData(item);
			if (metadata != null) {
				addItemMetadata(doc, metadata);
				JsonNode titleData = _docCache.getTitleMetaData(item);
				if (titleData != null) {
					addTitleMetadata(doc, titleData);
				}
			}

			try {
				server.add(doc);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private void addItemMetadata(SolrInputDocument doc, JsonNode metadata) {
		String year = metadata.get("Year").getTextValue();
		if (!StringUtils.isEmpty(year)) {
			YearRange range = parseYearRange(year);
			if (range != null) {
				doc.addField("startYear", range.startYear);
				doc.addField("endYear", range.endYear);
			}
		}

		addField(metadata, "Volume", "volume", doc);
		addField(metadata, "Contributor", "contributor", doc);
		addField(metadata, "Source", "source", doc);

		int titleId = metadata.get("PrimaryTitleID").asInt();
		doc.addField("titleId", titleId);

	}

	private void addTitleMetadata(SolrInputDocument doc, JsonNode titleData) {
		// Full title
		addField(titleData, "FullTitle", "fullTitle", doc);
		// Publisher Name
		addField(titleData, "PublisherName", "publisherName", doc);
		// Publisher Place
		addField(titleData, "PublisherPlace", "publisherPlace", doc);

		// Author(s)
		for (String author: selectField(titleData.get("Authors"), "Name")) {
			doc.addField("author", author);
		}
		
		// Author ID
		for (String authorId: selectField(titleData.get("Authors"), "CreatorID")) {
			doc.addField("authorId", authorId);
		}

		// Subject(s)
		for (String subject : selectField(titleData.get("Subjects"), "SubjectText")) {
			doc.addField("subject", subject);
		}

		// Publication Dates
		String date = titleData.get("PublicationDate").getTextValue();
		YearRange range = parseYearRange(date);
		if (range != null) {
			doc.addField("publicationStartYear", range.startYear);
			doc.addField("publicationEndYear", range.startYear);
		}

	}

	private void addField(JsonNode obj, String jsonfield, String indexField, SolrInputDocument doc) {
		String value = obj.get(jsonfield).getTextValue();
		if (!StringUtils.isEmpty(value)) {
			doc.addField(indexField, value);
		}
	}

	private List<String> selectField(JsonNode parent, String textField) {
		ArrayList<String> results = new ArrayList<String>();
		if (parent.isArray()) {
			for (JsonNode child : parent) {
				String name = child.get(textField).asText();
				if (name != null) {					
					if (!StringUtils.isEmpty(name)) {
						results.add(name);
					}
				}
			}
		}
		return results;
	}

	public static YearRange parseYearRange(String range) {
		
		if (StringUtils.isEmpty(range)) {
			return null;
		}

		if (StringUtils.isNumeric(range)) {
			return new YearRange(range, range);
		}

		// Look for YYYY-YYYY
		Matcher m = YEAR_RANGE_PATTERN.matcher(range);
		if (m.find()) {
			return new YearRange(m.group(1), m.group(2));
		}

		// Look for YYYY-YY
		m = ABBREV_RANGE_PATTERN.matcher(range);
		if (m.find()) {
			String start = m.group(1);
			return new YearRange(start, start.substring(0, 2) + m.group(2));
		}

		// Look for any for 4 consecutive digits!
		m = SINGLE_YEAR_PATTERN.matcher(range);
		if (m.find()) {
			return new YearRange(m.group(1), m.group(1));
		}

		return null;
	}

	public static class YearRange {

		public YearRange() {
		}

		public YearRange(int start, int end) {
			startYear = start;
			endYear = end;
		}

		public YearRange(String start, String end) {
			startYear = Integer.parseInt(start);
			endYear = Integer.parseInt(end);
		}

		public int startYear;
		public int endYear;
	}

}
