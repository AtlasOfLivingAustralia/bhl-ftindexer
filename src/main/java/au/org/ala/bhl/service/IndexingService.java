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
import au.org.ala.bhl.TaxonGrab;

/**
 * Service object that facades the process of interacting with the SOLR index
 * 
 * @author baird
 *
 */
public class IndexingService extends AbstractService {

	private final String _serverURL;
	private TaxonGrab _taxonGrab;
	private DocumentCacheService _docCache;

	private static Pattern PAGE_FILE_REGEX = Pattern.compile("^(\\d{5})_(\\d+).txt$");

	/**
	 * CTOR
	 * 
	 * @param serverUrl
	 * @param docCache
	 */
	public IndexingService(String serverUrl, DocumentCacheService docCache) {
		_serverURL = serverUrl;		
		_taxonGrab = new TaxonGrab();
		_docCache = docCache;
	}

	/**
	 * Create a new instance of the Solr server API facade
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
				String year = metadata.get("Year").getTextValue();
				if (!StringUtils.isEmpty(year)) {
					doc.addField("year", year);
				}
				
				String volume = metadata.get("Volume").getTextValue();
				if (!StringUtils.isEmpty(volume)) {
					doc.addField("volume", volume);
				}
				
				String contributor = metadata.get("Contributor").getTextValue();
				if (!StringUtils.isEmpty(contributor)) {
					doc.addField("contributor", contributor);
				}
				
				String source = metadata.get("Source").getTextValue();
				if (!StringUtils.isEmpty(source)) {
					doc.addField("source", source);
				}
				
			}

			// String language = "english";
			//
			// CacheControlBlock ccb =
			// _docCache.getCacheControl(item.getInternetArchiveId());
			// if (ccb != null && !StringUtils.isEmpty(ccb.Language)) {
			// language = ccb.Language;
			// }
			//
			// LanguageScore score = WordLists.detectLanguage(pageText,
			// language);
			// String lang = language;
			// if (score != null && !
			// StringUtils.equalsIgnoreCase(score.getName(), language) &&
			// score.getScore() > .75) {
			// log("Page %s - %s language detected as %s (scored %g) - This conflicts with meta data language of %s",
			// item.getItemId(), pageId, score.getName(), score.getScore(),
			// language);
			// lang = score.getName();
			// }
			//
			// List<String> names = _taxonGrab.findNames(pageText, lang);
			// if (names.size() > 0) {
			// String namesStr = StringUtils.join(names, ",");
			// doc.addField("taxonNames", namesStr);
			// log("Names detected in page %s (%s) : %s", pageId,
			// item.getInternetArchiveId(), namesStr);
			// }

			try {
				server.add(doc);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

}
