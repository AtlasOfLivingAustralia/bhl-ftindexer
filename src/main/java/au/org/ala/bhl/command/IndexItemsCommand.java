package au.org.ala.bhl.command;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.IndexingController;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.policy.DefaultRetrievePolicy;
import au.org.ala.bhl.policy.ItemRetrievePolicy;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

@Command(name = "index-items")
public class IndexItemsCommand extends AbstractCommand {

    private static IndexingController _indexer;

    private static ItemRetrievePolicy _retrievePolicy = new DefaultRetrievePolicy();

    public void execute(final ItemsService service, final IndexerOptions indexerOptions) throws Exception {

        final DocumentCacheService docCache = new DocumentCacheService(indexerOptions.getDocCachePath());

        _indexer = new IndexingController(indexerOptions);
        service.forAllItems(new ItemTOHandler() {
            public void onItem(ItemTO item) {

                if (indexerOptions.getIndexDocCacheOnly()) {
                    if (!StringUtils.isEmpty(item.getLocalCacheFile())) {
                        _indexer.indexItem(createItemDescriptor(item), item.getLocalCacheFile());
                    }
                } else {

                    if (StringUtils.isNotEmpty(item.getStatus()) && item.getStatus().equals(ItemStatus.FETCHED) && docCache.isItemInCache(item)) {
                        _indexer.indexItem(createItemDescriptor(item), item.getLocalCacheFile());
                    } else if (_retrievePolicy.shouldRetrieveItem(item)) {
                        _indexer.retrieveAndIndexItem(createItemDescriptor(item));
                    } else {
                        log("Skipping item %s (status: %s)", item.getItemId(), item.getStatus());
                    }
                }
            }
        });
        log("Jobs queued, Waiting...");
    }

    public void defineOptions(Options options) {
        options.addOption("indexlocalonly", true, "Index the documents already in the local document cache only");
        options.addOption("solrserver", true, "URL for the SOLR instance (index-items)");
        options.addOption("retrievethreads", true, "The number of document retrieving threads (index-items)");
        options.addOption("indexingthreads", true, "The number of indexing threads (index-items)");
    }

}
