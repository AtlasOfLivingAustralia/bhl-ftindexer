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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.NullNode;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.Timer;
import au.org.ala.bhl.to.ItemTO;

/**
 * Service class representing the document cache
 * 
 * @author baird
 * 
 */
public class DocumentCacheService extends AbstractService {

	private String _cacheDir;
	public static Pattern PAGE_FILE_REGEX = Pattern.compile("^(\\d{5})_(\\d+).txt$");
	private ObjectMapper _objectMapper;
	private static String SEPARATOR = System.getProperty("file.separator");

	/**
	 * CTOR
	 * 
	 * @param cacheDir
	 */
	public DocumentCacheService(String cacheDir) {
		_cacheDir = cacheDir;
		_objectMapper = new ObjectMapper();
	}

	/**
	 * Tests to see if an item exists already in the cache
	 * 
	 * @param item
	 * @return
	 */
	public boolean isItemInCache(ItemTO item) {
		String path = getItemDirectoryPath(item);
		File f = new File(path);
		return f.exists();
	}

	/**
	 * return the base path for the document cache
	 * 
	 * @return
	 */
	public String getDocumentCachePath() {
		return _cacheDir;
	}

	/**
	 * returns the path for a specific item identified by its Internet Archive ID
	 * 
	 * @param iaId
	 * @return
	 */
	public String getItemDirectoryPath(String iaId) {
		String subdir = iaId.substring(0, 1).toLowerCase();
		return String.format("%s%s%s%s%s", _cacheDir, SEPARATOR, subdir, SEPARATOR, iaId);
	}

	/**
	 * returns the path for a specific item
	 * 
	 * @param item
	 * @return
	 */
	public String getItemDirectoryPath(ItemTO item) {
		String subdir = item.getInternetArchiveId().substring(0, 1).toLowerCase();
		return String.format("%s%s%s%s%s", _cacheDir, SEPARATOR, subdir, SEPARATOR, item.getInternetArchiveId());
	}

	/**
	 * Visits each item in the cache
	 * 
	 * @param handler
	 */
	public void forEachItem(final CachedItemHandler handler) {
		if (handler == null) {
			return;
		}

		File topLevel = new File(_cacheDir);

		if (topLevel.exists() && topLevel.isDirectory()) {
			int itemCount = countItems(topLevel);
			log("Traversing item cache (%d partitions)...", itemCount);
			FileItemAdaptor h = new FileItemAdaptor(handler, itemCount);
			Timer t = new Timer("Traversing items in cache");
			String[] partitions = topLevel.list();
			for (String partition : partitions) {
				File partDir = new File(topLevel.getAbsolutePath() + SEPARATOR + partition);
				if (partDir.exists() && partDir.isDirectory()) {
					log("Traversing partition '%s'...", partition);
					partDir.listFiles(h);
				}
			}
			t.stop(true, false, String.format("%d items traversed.", h.getCount()));
		}
	}

	private int countItems(File topLevel) {
		int total = 0;
		File[] files = topLevel.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				total += file.listFiles().length;
			}
		}
		return total;
	}

	/**
	 * Visits each page for each item in the cache
	 * 
	 * @param handler
	 */
	public void forEachItemPage(final CachedItemPageHandler handler) {

		if (handler == null) {
			return;
		}

		ItemPageHandlerAdapter adapter = new ItemPageHandlerAdapter(handler);
		Timer t = new Timer("Traversing pages");
		forEachItem(adapter);
		t.stop(true, false, String.format("%d pages traversed.", adapter.getPageCount()));
	}

	public void forEachItemPage(ItemDescriptor item, CachedItemPageHandler handler) {
		ItemPageHandlerAdapter adapter = new ItemPageHandlerAdapter(handler);
		adapter.onItem(new File(getItemDirectoryPath(item.getInternetArchiveId())));
	}

	public void createCacheControl(ItemDescriptor item, boolean force) {

		try {
			String itemPath = getItemDirectoryPath(item.getInternetArchiveId());

			String completeFilePath = String.format("%s%s.complete", itemPath, SEPARATOR);
			File completeFile = new File(completeFilePath);
			String ccbpath = String.format("%s%s.cachecontrol", itemPath, SEPARATOR);
			File ccbfile = new File(ccbpath);

			CacheControlBlock ccb = new CacheControlBlock();
			ccb.ItemID = item.getItemId();
			ccb.InternetArchiveID = item.getInternetArchiveId();

			if (completeFile.exists()) {
				String date = FileUtils.readFileToString(completeFile).trim();
				if (!StringUtils.isEmpty(date)) {
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zz yyyy");
					ccb.TimeComplete = sdf.parse(date.trim());
				}
			}

			if (!ccbfile.exists() || force) {

				if (ccbfile.exists()) {
					ccb = _objectMapper.readValue(ccbfile, CacheControlBlock.class);
				}

				JsonNode node = WebServiceHelper.getJSON(item.getItemMetaDataURL());
				boolean ok = false;
				if (node != null) {
					JsonNode result = node.get("Result");
					if (result != null && !(result instanceof NullNode)) {
						ccb.Language = result.get("Language").getTextValue();
						ccb.ItemURL = result.get("ItemUrl").getTextValue();

						int titleId = result.get("PrimaryTitleID").getIntValue();

						ccb.PrimaryTitleID = "" + titleId;

						JsonNode titleRoot = WebServiceHelper.getJSON(item.getTitleMetaDataURL(titleId, false));
						JsonNode titleResult = titleRoot.get("Result");

						log("Writing cache control for item %s (%s)", item.getItemId(), item.getInternetArchiveId());
						_objectMapper.writeValue(ccbfile, ccb);
						_objectMapper.writeValue(new File(String.format("%s%s.metadata", itemPath, SEPARATOR)), result);
						_objectMapper.writeValue(new File(String.format("%s%s.titlemetadata", itemPath, SEPARATOR)), titleResult);

						ok = true;
					}
				}

				if (!ok) {
					// skip?
					log("Meta data for item %s (%s) could not be retrieved. Writing dummy cache control to prevent retries.", item.getItemId(), item.getInternetArchiveId());
					ccb.ItemURL = "";
					ccb.Language = "";
					_objectMapper.writeValue(ccbfile, ccb);
				} else {
					if (completeFile.exists()) {
						log("Deleting obselete .complete file for item %s (%s).", item.getItemId(), item.getInternetArchiveId());
						completeFile.delete();
					}
				}
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}

	public boolean isItemComplete(ItemDescriptor item) {
		final String iaId = item.getInternetArchiveId();
		String itemDir = getItemDirectoryPath(iaId);
		// String completeFilePath = String.format("%s%s.complete", itemDir,
		// SEPARATOR);
		// File completeFile = new File(completeFilePath);
		CacheControlBlock ccb = getCacheControl(item.getInternetArchiveId());

		File documentDir = new File(itemDir);

		if (ccb != null && documentDir.exists()) {
			return true;
		}

		return false;
	}

	/**
	 * Retrieves the text for an item from the BHL and stores it in the cache
	 * 
	 * @param item
	 * @param forceOverwrite
	 */
	public void retrieveItem(ItemDescriptor item, boolean forceOverwrite) {
		final String iaId = item.getInternetArchiveId();
		String itemDir = getItemDirectoryPath(iaId);
		// String completeFilePath = String.format("%s%s.complete", itemDir,
		// SEPARATOR);
		// File completeFile = new File(completeFilePath);
		CacheControlBlock ccb = getCacheControl(item.getInternetArchiveId());

		File documentDir = new File(itemDir);

		if (documentDir.exists() && forceOverwrite) {
			documentDir.delete();
		}

		if (ccb != null && documentDir.exists()) {
			log("Cache control block already exists for item %s. Skipping retrieve.", item.getItemId());
			return;
		}

		try {
			log("Retrieving missing or incomplete item %s (IA: %s)", item.getItemId(), item.getInternetArchiveId());
			JsonNode node = WebServiceHelper.getJSON(item.getItemMetaDataURL());
			if (node != null) {
				if (!documentDir.exists()) {
					log("Creating directory: %s", documentDir.getAbsoluteFile());
					documentDir.mkdir();
				}

				downloadItemPages(node, item, itemDir);

				getItemsService().setItemStatus(item.getItemId(), ItemStatus.FETCHED, 0);
				createCacheControl(item, true);
			} else {
				log("Failed to get item meta data from BHL-AU for item %s", item.getItemId());
			}

			getItemsService().setItemLocalPath(item.getItemId(), itemDir);

		} catch (Exception ex) {
			log(ex.getMessage());
		}

	}

	/**
	 * Downloads and stores pages of text for an item
	 * 
	 * @param root
	 * @param item
	 * @param itemDir
	 * @return
	 * @throws IOException
	 */
	private boolean downloadItemPages(JsonNode root, ItemDescriptor item, String itemDir) throws IOException {
		JsonNode pagesNode = root.path("Result").path("Pages");
		if (pagesNode != null && pagesNode.isArray() && pagesNode.size() > 0) {
			int pageCount = 0;
			int skipCount = 0;

			for (int i = 0; i < pagesNode.size(); ++i) {
				JsonNode node = pagesNode.get(i);
				int pageId = node.get("PageID").getIntValue();
				String pagePath = String.format("%s%s%05d_%d.txt", itemDir, SEPARATOR, i, pageId);
				File pageFile = new File(pagePath);
				if (!pageFile.exists()) {
					String ocrURL = node.get("OcrUrl").getTextValue();
					if (StringUtils.isNotEmpty(ocrURL)) {
						log("Retrieving page %d of %d (Page ID %d for item %s)", i + 1, pagesNode.size(), pageId, item.getItemId());
						String ocr = WebServiceHelper.getText(ocrURL);
						FileUtils.writeStringToFile(pageFile, ocr);
						pageCount++;
					} else {
						log("OCR text is empty for item %s (IA: %s)", item.getItemId(), item.getInternetArchiveId());
					}
				} else {
					skipCount++;
				}

			}

			compressPages(item);

			log("Item text retrieved for item %s (IA: %s) - %d pages of OCR retrieved, %d existing pages skipped.", item.getItemId(), item.getInternetArchiveId(), pageCount, skipCount);
			return true;
		} else {
			log("No pages found for item %s (IA: %s). Skipping.", item.getItemId(), item.getInternetArchiveId());
		}
		return false;
	}

	/**
	 * Gets an items metadata file from the document cache
	 * 
	 * @param item
	 * @return
	 */
	public JsonNode getItemMetaData(ItemDescriptor item) {
		String itemPath = getItemDirectoryPath(item.getInternetArchiveId());
		return getJSONFile(String.format("%s%s.metadata", itemPath, SEPARATOR));
	}

	public JsonNode getItemMetaData(String iaId) {
		String itemPath = getItemDirectoryPath(iaId);
		return getJSONFile(String.format("%s%s.metadata", itemPath, SEPARATOR));
	}

	public JsonNode getTitleMetaData(ItemDescriptor item) {
		String itemPath = getItemDirectoryPath(item.getInternetArchiveId());
		return getJSONFile(String.format("%s%s.titlemetadata", itemPath, SEPARATOR));
	}

	public JsonNode getTitleMetaData(String iaId) {
		String itemPath = getItemDirectoryPath(iaId);
		return getJSONFile(String.format("%s%s.titlemetadata", itemPath, SEPARATOR));
	}

	protected JsonNode getJSONFile(String path) {
		try {
			File f = new File(path);
			if (f.exists()) {
				String text = FileUtils.readFileToString(f);
				JsonNode root = new ObjectMapper().readValue(text, JsonNode.class);
				return root;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	class ItemPageHandlerAdapter implements CachedItemHandler {

		private CachedItemPageHandler _handler;
		private int _pageCountTotal;

		public ItemPageHandlerAdapter(CachedItemPageHandler handler) {
			_handler = handler;
		}

		private static final int BUFFER_SIZE = 2048;

		public void onItem(File itemDir) {

			String iaId = itemDir.getName();
			File f = getPageArchiveFile(iaId);
			if (f.exists()) {
				try {
					ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(f)));
					_handler.startItem(iaId);
					int pageCount = 0;

					ZipEntry entry;
					while ((entry = zin.getNextEntry()) != null) {

						Matcher m = PAGE_FILE_REGEX.matcher(entry.getName());
						if (m.matches()) {
							String pageId = m.group(2);
							pageCount++;

							if (_handler != null) {
								int count;
								int size = (int) entry.getSize();
								if (size < 0) {
									size = 2048;
								}
								ByteArrayOutputStream dest = new ByteArrayOutputStream(size);
								byte data[] = new byte[BUFFER_SIZE];
								while ((count = zin.read(data, 0, BUFFER_SIZE)) != -1) {
									dest.write(data, 0, count);
								}
								dest.close();
								_handler.onPage(iaId, pageId, dest.toString("utf-8"));
							}
						}

					}
					_handler.endItem(iaId);
					log("%d pages processed for item %s", pageCount, itemDir.getName());
					_pageCountTotal += pageCount;
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		public int getPageCount() {
			return _pageCountTotal;
		}

		public void onProgress(int countComplete, int percentComplete) {
			LogService.log(DocumentCacheService.class, "%d items processed (%d%% complete).", countComplete, percentComplete);
		}
	}

	class FileItemAdaptor implements FileFilter {

		private CachedItemHandler _handler;
		private int _count;
		private int _total;
		private int _lastPercent = 0;

		public FileItemAdaptor(CachedItemHandler handler, int itemCount) {
			_handler = handler;
			_total = itemCount;
		}

		public int getCount() {
			return _count;
		}

		public boolean accept(File dir) {
			_count++;
			if (_handler != null) {
				_handler.onItem(dir);
				int percent = Math.round(((float) _count / (float) _total) * (float) 100.0);
				if (percent != _lastPercent) {
					_lastPercent = percent;
					if (_handler != null) {
						_handler.onProgress(_count, _lastPercent);
					}
				}

			}

			return false;
		}
	}

	public boolean pageArchiveExists(ItemDescriptor item) {
		String path = String.format("%s%spages.zip", getItemDirectoryPath(item.getInternetArchiveId()), SEPARATOR);
		File file = new File(path);
		return file.exists();
	}

	public CacheControlBlock getCacheControl(String internetArchiveId) {
		String ccbpath = String.format("%s%s.cachecontrol", getItemDirectoryPath(internetArchiveId), SEPARATOR);
		File ccbfile = new File(ccbpath);
		if (ccbfile.exists()) {
			try {
				return (CacheControlBlock) _objectMapper.readValue(ccbfile, CacheControlBlock.class);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		return null;
	}

	public File getPageArchiveFile(ItemDescriptor item) {
		return getPageArchiveFile(item.getInternetArchiveId());
	}

	public File getPageArchiveFile(String iaId) {
		return new File(String.format("%s%spages.zip", getItemDirectoryPath(iaId), SEPARATOR));
	}

	public void compressPages(ItemDescriptor itemDesc) {
		File itemDir = new File(getItemDirectoryPath(itemDesc.getInternetArchiveId()));
		File file = getPageArchiveFile(itemDesc);
		if (file.exists()) {
			log("Deleting existing archive file: %s", file.getAbsolutePath());
			file.delete();
		}
		try {
			File[] candidates = itemDir.listFiles();
			int pageCount = 0;

			ZipOutputStream out = null;

			for (File candidate : candidates) {
				Matcher m = PAGE_FILE_REGEX.matcher(candidate.getName());
				if (m.matches()) {
					if (out == null) {
						out = new ZipOutputStream(new FileOutputStream(file));
					}
					pageCount++;
					FileInputStream in = new FileInputStream(candidate);
					out.putNextEntry(new ZipEntry(candidate.getName()));
					byte[] buf = new byte[2048];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.closeEntry();
					in.close();

					candidate.delete();
				}
			}

			if (out != null) {
				out.close();
				log("%d pages add to pages.zip for item %s", pageCount, itemDesc);
			} else {
				log("No pages for item %s", itemDesc);
			}

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
