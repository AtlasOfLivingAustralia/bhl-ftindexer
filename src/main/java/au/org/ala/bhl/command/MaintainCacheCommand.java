package au.org.ala.bhl.command;

import java.io.File;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemSourceService;
import au.org.ala.bhl.to.ItemTO;

@Command(name = "maintain-cache")
public class MaintainCacheCommand extends AbstractCommand {

    public void execute(final ItemSourceService service, final IndexerOptions options) throws Exception {
        
        final DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());

        service.forAllItems(new ItemTOHandler() {

            public void onItem(ItemTO item) {                
                File f = new File(docCache.getItemDirectoryPath(item));
                log("Updating local file path for item %s", item.getInternetArchiveId());
                if (f.exists()) {
                    service.updateCachedFileInfo(item.getItemId(), f);
                    if (StringUtils.isNotEmpty(item.getStatus()) && !item.getStatus().equals(ItemStatus.INDEXED)) {
                        service.setItemStatus(item.getItemId(), ItemStatus.FETCHED, 0);
                    }
                } else {
                    service.setItemLocalPath(item.getItemId(), "");
                    service.setItemStatus(item.getItemId(), null, 0);
                }
            }
        });
    }

    public void defineOptions(Options options) {
    }

}
