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
package au.org.ala.bhl;

import org.apache.commons.cli.CommandLine;

/**
 * Transfer object holding the current state of the programs options.
 * 
 * Options are read from the command line upon construction
 * 
 * @author baird
 *
 */
public class IndexerOptions {
    
    private boolean _indexDocCacheOnly;
    private String _sourceFilename;
    private String _docCachePath;
    private String _solrServerUrl;  
    private String _outputFile;    
    private int _jobThreads;    
    private String _itemFilter;
    private boolean _force;
    private boolean _clear;
    private String _script;
    
    public IndexerOptions(CommandLine line) {
        
        _indexDocCacheOnly = Boolean.parseBoolean(line.getOptionValue("indexlocalonly", "false"));
        
        _sourceFilename = line.getOptionValue("sourcefile", "BHLExtract.csv");
        
        _docCachePath = line.getOptionValue("doccache", "/data/bhl-ftindex/doccache");
        _solrServerUrl = line.getOptionValue("solrserver", "http://localhost:8983/solr");
        _jobThreads = Integer.parseInt(line.getOptionValue("threads", "1"));
        
        _outputFile = line.getOptionValue("o", "out.txt");
        
        _itemFilter = line.getOptionValue("filter", "");
        
        _force = line.hasOption("force");
        
        _clear = line.hasOption("clear");
        
        _script = line.getOptionValue("script", "");
        
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
       
    public int getThreadCount() {
    	return _jobThreads;
    }
        
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
    
    public boolean setForce(boolean force) {
    	return _force;
    }
    
    public boolean getForce() {
    	return _force;
    }
    
    public boolean getClear() {
    	return _clear;
    }
    
    public void setClear(boolean val) {
    	_clear = val;
    }
    
    public String getScript() {
    	return _script;
    }

}
