package au.org.ala.bhl;

import org.apache.commons.cli.CommandLine;

public class IndexerOptions {
    
    private boolean _indexDocCacheOnly;
    private String _sourceFilename;
    private String _docCachePath;
    private String _solrServerUrl;
    
    private int _retrieveThreads;
    private int _indexingThreads;
    
    public IndexerOptions(CommandLine line) {
        
        _indexDocCacheOnly = Boolean.parseBoolean(line.getOptionValue("indexlocalonly", "false"));
        
        _sourceFilename = line.getOptionValue("sourcefile", "c:\\zz\\BHLExtract.csv");
        
        _docCachePath = line.getOptionValue("doccache", "J:\\data\\DocCache");
        _solrServerUrl = line.getOptionValue("solrserver", "http://localhost:8983/solr");
        _retrieveThreads = Integer.parseInt(line.getOptionValue("retrievethreads", "20"));
        _indexingThreads = Integer.parseInt(line.getOptionValue("indexingthreads", "1"));
    }
    
    
    public boolean getIndexDocCacheOnly() {
        return _indexDocCacheOnly;
    }
    
    public void setIndexDocCacheOnly(boolean value) {
        _indexDocCacheOnly = value;
    }
    
    public String getSourceFilename() {
        return _sourceFilename;
    }
    
    public void setSourceFilename(String filename) {
        _sourceFilename = filename;
    }
    
    public String getDocCachePath() {
        return _docCachePath;
    }
    
    public void setDocCachePath(String path) {
        _docCachePath = path;
    }
    
    public String getSolrServerURL() {
        return _solrServerUrl;
    }
    
    public void setSolrServerURL(String url) {
        _solrServerUrl = url;
    }
    
    public int getRetrieveThreadCount() {
        return _retrieveThreads;
    }
    
    public void setRetrieveThreadCount(int count) {
        _retrieveThreads = count;
    }
    
    public int getIndexerThreadCount() {
        return _indexingThreads;
    }
    
    public void setIndexerThreadCount(int count) {
        _indexingThreads = count;
    }

}
