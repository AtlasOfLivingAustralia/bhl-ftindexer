package au.org.ala.bhl.service;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.NullNode;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.Timer;
import au.org.ala.bhl.to.ItemTO;

public class DocumentCacheService {

	private String _cacheDir;
	public static Pattern PAGE_FILE_REGEX = Pattern.compile("^(\\d{5})_(\\d+).txt$");
	private ObjectMapper _objectMapper;
	private static String SEPARATOR = System.getProperty("file.separator");

	public DocumentCacheService(String cacheDir) {
		_cacheDir = cacheDir;
		_objectMapper = new ObjectMapper();
	}

	public boolean isItemInCache(ItemTO item) {
		String path = getItemDirectoryPath(item);
		File f = new File(path);
		return f.exists();
	}

	public String getDocumentCachePath() {
		return _cacheDir;
	}

	public String getItemDirectoryPath(String iaId) {
		return String.format("%s%s%s", _cacheDir, SEPARATOR, iaId);
	}

	public String getItemDirectoryPath(ItemTO item) {
		return String.format("%s%s%s", _cacheDir, SEPARATOR, item.getInternetArchiveId());
	}

	protected void log(String format, Object... args) {
		LogService.log(H2Service.class, format, args);
	}

	public void forEachItem(final CachedItemHandler handler) {
		if (handler == null) {
			return;
		}

		File topLevel = new File(_cacheDir);

		if (topLevel.exists() && topLevel.isDirectory()) {
			int itemCount = topLevel.list().length;
			log("Traversing item cache (%d items)...", itemCount);
			FileItemAdaptor h = new FileItemAdaptor(handler, itemCount);
			Timer t = new Timer("Traversing items in cache");
			topLevel.listFiles(h);
			t.stop(true, false, String.format("%d items traversed.", h.getCount()));
		}
	}

	public void forEachItemPage(final CachedItemPageHandler handler) {

		if (handler == null) {
			return;
		}

		ItemPageHandlerAdapter adapter = new ItemPageHandlerAdapter(handler);
		Timer t = new Timer("Traversing pages");
		forEachItem(adapter);
		t.stop(true, false, String.format("%d pages traversed.", adapter.getPageCount()));
	}

	public void createCacheControl(ItemDescriptor item) {

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
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zz YYYY");
					ccb.TimeComplete = sdf.parse(date.trim());
				}
			}

			boolean force = false;

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
						log("Writing cache control for item %s (%s)", item.getItemId(), item.getInternetArchiveId());
						_objectMapper.writeValue(ccbfile, ccb);
						_objectMapper.writeValue(new File(String.format("%s%s.metadata", itemPath, SEPARATOR)), result);
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

	class ItemPageHandlerAdapter implements CachedItemHandler {

		private CachedItemPageHandler _handler;
		private int _pageCountTotal;

		public ItemPageHandlerAdapter(CachedItemPageHandler handler) {
			_handler = handler;
		}

		public void onItem(File itemDir) {
			File[] candidates = itemDir.listFiles();
			int pageCount = 0;
			String itemId = itemDir.getName();
			_handler.startItem(itemId);
			for (File candidate : candidates) {
				Matcher m = PAGE_FILE_REGEX.matcher(candidate.getName());
				if (m.matches()) {
					String pageId = m.group(2);
					pageCount++;
					if (_handler != null) {
						_handler.onPage(itemId, pageId, candidate);
					}
				}
			}
			_handler.endItem(itemId);
			log("%d pages processed for item %s", pageCount, itemDir.getName());
			_pageCountTotal += pageCount;
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

	public CacheControlBlock getCacheControl(String itemId) {
		String ccbpath = String.format("%s%s%s%s.cachecontrol", _cacheDir, SEPARATOR, itemId, SEPARATOR);
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

}
