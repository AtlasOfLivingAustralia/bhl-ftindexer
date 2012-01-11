package au.org.ala.bhl;

import org.apache.commons.cli.CommandLine;

public class IndexerOptions {
    
    private boolean _indexDocCacheOnly;
    private String _sourceFilename;
    private String _docCachePath;
    private String _solrServerUrl;  
    private String _outputFile;    
    private int _jobThreads;    
    private String _itemFilter;
    
    public IndexerOptions(CommandLine line) {
        
        _indexDocCacheOnly = Boolean.parseBoolean(line.getOptionValue("indexlocalonly", "false"));
        
        _sourceFilename = line.getOptionValue("sourcefile", "BHLExtract.csv");
        
        _docCachePath = line.getOptionValue("doccache", "/data/bhl-ftindex/doccache");
        _solrServerUrl = line.getOptionValue("solrserver", "http://localhost:8983/solr");
        _jobThreads = Integer.parseInt(line.getOptionValue("threads", "1"));
        
        _outputFile = line.getOptionValue("o", "out.txt");
        
        _itemFilter = line.getOptionValue("filter", "");
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
    
//    public int getRetrieveThreadCount() {
//        return _retrieveThreads;
//    }
//    
//    public void setRetrieveThreadCount(int count) {
//        _retrieveThreads = count;
//    }
//    
//    public int getIndexerThreadCount() {
//        return _indexingThreads;
//    }
    
    public int getThreadCount() {
    	return _jobThreads;
    }
    
//    public void setIndexerThreadCount(int count) {
//        _indexingThreads = count;
//    }
    
    public String getOutputFile() {
    	return _outputFile;
    }
    
    public void setOutputFile(String outputFile) {
    	_outputFile = outputFile;    			
    }
    
    public String getItemFilter() {
    	return _itemFilter;
    }
    
    public void setItemFilter(String regex) {
    	_itemFilter = regex;
    }

}
