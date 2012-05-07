package au.org.ala.bhl.command;

import org.apache.commons.cli.Options;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

@Command(name="compress-pages")
public class CompressPagesCommand extends AbstractCommand {

	public void execute(final ItemsService service, final IndexerOptions options) throws Exception {
		
		final DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());
		
		service.forAllItems(new ItemTOHandler() {
			
			public void onItem(ItemTO item) {
				if (docCache.isItemInCache(item)) {
					ItemDescriptor itemDesc = createItemDescriptor(item);
					log("Found item in cache: %s", item);
					if (!docCache.isItemComplete(itemDesc)) {
						log("Retrieving incomplete item before compressing...(%s)", item.getInternetArchiveId(), item);
						docCache.retrieveItem(itemDesc, false);	
					}
					
					boolean force = options.getForce();
					if (force || !docCache.pageArchiveExists(itemDesc)) {
						docCache.compressPages(itemDesc);
					}
				}
			}
		}, createItemFilter(options));
		
		log("Done");
	}

	public void defineOptions(Options options) {
		// TODO Auto-generated method stub

	}

}
