package au.org.ala.bhl.service;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.Timer;
import au.org.ala.bhl.to.ItemTO;

public class DocumentCacheService {

	private String _cacheDir;
	public static Pattern PAGE_FILE_REGEX = Pattern.compile("^(\\d{5})_(\\d+).txt$");

	public DocumentCacheService(String cacheDir) {
		_cacheDir = cacheDir;
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
		return String.format("%s\\%s", _cacheDir, iaId);
	}

	public String getItemDirectoryPath(ItemTO item) {
		return String.format("%s\\%s", _cacheDir, item.getInternetArchiveId());
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

			String completeFilePath = String.format("%s\\.complete", itemPath);
			File completeFile = new File(completeFilePath);
			
			CacheControlBlock ccb = new CacheControlBlock();
			if (completeFile.exists()) {
				String date = FileUtils.readFileToString(completeFile).trim();
				if (!StringUtils.isEmpty(date)) {				
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zz YYYY");				
					ccb.TimeComplete = sdf.parse(date.trim());
				}
			}
			
			JsonNode node = WebServiceHelper.getJSON(item.getItemMetaDataURL());
			
			
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
			for (File candidate : candidates) {
				Matcher m = PAGE_FILE_REGEX.matcher(candidate.getName());
				if (m.matches()) {
					String itemId = itemDir.getName();
					String pageId = m.group(2);
					pageCount++;
					if (_handler != null) {
						_handler.onPage(itemId, pageId, candidate);
					}
				}
			}
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
				int percent = Math.round(((float) _count / (float) _total) / (float) 100.0);
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

}
