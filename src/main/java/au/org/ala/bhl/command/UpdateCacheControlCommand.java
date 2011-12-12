package au.org.ala.bhl.command;

import org.apache.commons.cli.Options;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.IndexingController;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

@Command(name = "update-cache-control")
public class UpdateCacheControlCommand extends AbstractCommand {

	public void execute(ItemsService service, IndexerOptions options) throws Exception {

		final IndexingController indexer = new IndexingController(options);

		service.forAllItems(new ItemTOHandler() {
			public void onItem(ItemTO item) {
				indexer.updateCacheControl(createItemDescriptor(item));
			}
		});
		indexer.queueStopMessages();
		log("Jobs queued, Waiting...");
	}

	public void defineOptions(Options options) {
	}

}
