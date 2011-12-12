package au.org.ala.bhl.command;

import org.apache.commons.cli.Options;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.IndexingController;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.policy.DefaultRetrievePolicy;
import au.org.ala.bhl.policy.ItemRetrievePolicy;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

@Command(name = "retrieve-items")
public class RetrieveItemsCommand extends AbstractCommand {

    private static IndexingController _indexer;

    private static ItemRetrievePolicy _retrievePolicy = new DefaultRetrievePolicy();

    public void execute(final ItemsService service, final IndexerOptions indexerOptions) throws Exception {

        _indexer = new IndexingController(indexerOptions);
        service.forAllItems(new ItemTOHandler() {
            public void onItem(ItemTO item) {

                if (_retrievePolicy.shouldRetrieveItem(item)) {
                    _indexer.retrieveItem(createItemDescriptor(item));
                }
            }
        });
        _indexer.queueStopMessages();
        log("Jobs queued, Waiting...");
    }

    public void defineOptions(Options options) {
        // TODO Auto-generated method stub

    }

}
