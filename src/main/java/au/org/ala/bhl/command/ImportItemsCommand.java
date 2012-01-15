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
package au.org.ala.bhl.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemDescriptorFilter;
import au.org.ala.bhl.ItemsFileHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.service.LogService;

@Command(name = "import-items")
public class ImportItemsCommand extends AbstractCommand {

    public void execute(final ItemsService service, final IndexerOptions options) throws Exception {
        
        if (StringUtils.isEmpty(options.getSourceFilename())) {
            throw new RuntimeException("No value for source file argument!");            
        }
        
        service.clearAllItems();        
        final DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());
        
        ItemImporter importer = new ItemImporter(service, docCache);
        
        processItemsFile(options, importer, createItemDescriptorFilter(options));        
        log("%d Items Imported.", importer.getItemCount());
    }
    
    private static ItemDescriptorFilter createItemDescriptorFilter(IndexerOptions options) {
    	if (!StringUtils.isEmpty(options.getItemFilter())) {
    		return new ItemDescriptorIAIdFilter(options.getItemFilter());
    	}
    	return null;
    }

    private static void processItemsFile(final IndexerOptions options, ItemsFileHandler handler, ItemDescriptorFilter filter) throws Exception {
        String sourceFile = options.getSourceFilename();
        File f = new File(sourceFile);
        if (f.exists()) {
            FileInputStream fis = new FileInputStream(f);
            Reader filereader = new InputStreamReader(fis, "ISO-8859-1");
            CSVReader reader = new CSVReader(filereader, ',', '"', 1);
            String[] nextLine;
            try {
                while ((nextLine = reader.readNext()) != null) {
                    ItemDescriptor item = new ItemDescriptor(nextLine[0], nextLine[1], nextLine[2], nextLine[3], nextLine[4]);
                    if (filter == null || filter.accept(item)) {
                    	LogService.log(ImportItemsCommand.class, "Including item %s", item.getInternetArchiveId());
	                    if (handler != null) {
	                        handler.onItem(item);
	                    }
                    }
                }
            } finally {
                reader.close();
                filereader.close();
                fis.close();
            }

        } else {
            throw new RuntimeException("File not found! " + sourceFile);
        }

    }

    public void defineOptions(Options options) {
        options.addOption("sourcefile", true, "Input file for seeding items store (import-items)");
        options.addOption("filter", true, "regex for filtering Internet Archive Ids");
    }
    
    class ItemImporter implements ItemsFileHandler {
        
        private DocumentCacheService _docCache;
        private ItemsService _service;
        
        private int _itemCount;
        
        public ItemImporter(ItemsService service, DocumentCacheService docCache) {
            _service = service;
            _docCache = docCache;
            _itemCount = 0;
        }

        public void onItem(ItemDescriptor item) {
            // log("Processing item: %s", item.getInternetArchiveId());
            _service.addItem(item);
            _itemCount++;
            String filename = _docCache.getItemDirectoryPath(item.getInternetArchiveId());
            File file = new File(filename);
            if (file.exists()) {
                log("Updating file cache info for item: %s", item.getItemId());
                _service.updateCachedFileInfo(item.getItemId(), file);
            }
        }
        
        public int getItemCount() {
            return _itemCount;
        }
        
    }
    
    static class ItemDescriptorIAIdFilter implements ItemDescriptorFilter {
    	
    	private Pattern _regex;

    	public ItemDescriptorIAIdFilter(String filter) {
    		_regex = Pattern.compile("^" + filter + "$");
    	}
    	
		public boolean accept(ItemDescriptor item) {			
			Matcher m = _regex.matcher(item.getInternetArchiveId());
			return m.find();
		}
    	
    }

}
