package au.org.ala.bhl.command;

import java.io.File;

import org.apache.commons.cli.Options;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

@Command(name = "reset-status")
public class ResetStatusCommand extends AbstractCommand {

	public void execute(final ItemsService service, final IndexerOptions options) throws Exception {
		
		final DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());
		
        service.forAllItems(new ItemTOHandler() {

            public void onItem(ItemTO item) {                
                File f = new File(docCache.getItemDirectoryPath(item));
                log("Updating status for item %s (%s)", item.getInternetArchiveId(), f.getAbsolutePath());
                if (f.exists()) {
                    service.updateCachedFileInfo(item.getItemId(), f);
                    service.setItemStatus(item.getItemId(), ItemStatus.FETCHED, 0);
                } else {
                    service.setItemLocalPath(item.getItemId(), "");
                    service.setItemStatus(item.getItemId(), null, 0);
                }
            }
        }, createItemFilter(options));
		
	}

	public void defineOptions(Options options) {
	}

}
