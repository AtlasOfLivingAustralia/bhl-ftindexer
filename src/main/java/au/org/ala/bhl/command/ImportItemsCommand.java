package au.org.ala.bhl.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemsFileHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemSourceService;

@Command(name = "import-items")
public class ImportItemsCommand extends AbstractCommand {

    public void execute(final ItemSourceService service, final IndexerOptions options) throws Exception {
        
        if (StringUtils.isEmpty(options.getSourceFilename())) {
            throw new RuntimeException("No value for source file argument!");            
        }
        
        service.clearAllItems();        
        final DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());
        
        ItemImporter importer = new ItemImporter(service, docCache);
        processItemsFile(options, importer);        
        log("%d Items Imported.", importer.getItemCount());
    }

    private static void processItemsFile(final IndexerOptions options, ItemsFileHandler handler) throws Exception {
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
                    if (handler != null) {
                        handler.onItem(item);
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
    }
    
    class ItemImporter implements ItemsFileHandler {
        
        private DocumentCacheService _docCache;
        private ItemSourceService _service;
        
        private int _itemCount;
        
        public ItemImporter(ItemSourceService service, DocumentCacheService docCache) {
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

}
