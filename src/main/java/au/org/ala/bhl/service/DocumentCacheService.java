package au.org.ala.bhl.service;

import java.io.File;

import au.org.ala.bhl.to.ItemTO;

public class DocumentCacheService {

    private String _cacheDir;

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

}
