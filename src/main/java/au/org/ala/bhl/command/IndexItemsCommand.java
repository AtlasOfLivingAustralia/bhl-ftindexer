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

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.IndexingController;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.WordLists;
import au.org.ala.bhl.policy.DefaultRetrievePolicy;
import au.org.ala.bhl.policy.ItemRetrievePolicy;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

/**
 * Traverses each item in the database a schedules an index-item job.
 * 
 * @author baird
 *
 */
@Command(name = "index-items")
public class IndexItemsCommand extends AbstractCommand {

    private static IndexingController _indexer;

    private static ItemRetrievePolicy _retrievePolicy = new DefaultRetrievePolicy();

    public void execute(final ItemsService service, final IndexerOptions indexerOptions) throws Exception {
    	
    	WordLists.loadWordLists();

        final DocumentCacheService docCache = new DocumentCacheService(indexerOptions.getDocCachePath());

        _indexer = new IndexingController(indexerOptions);
        service.forAllItems(new ItemTOHandler() {
            public void onItem(ItemTO item) {

                if (indexerOptions.getIndexDocCacheOnly()) {
                    if (!StringUtils.isEmpty(item.getLocalCacheFile()) && !item.getStatus().equals(ItemStatus.INDEXED)) {
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
        }, createItemFilter(indexerOptions));
        _indexer.queueStopMessages();
        log("Jobs queued, Waiting...");
    }

    public void defineOptions(Options options) {
        options.addOption("indexlocalonly", false, "Index the documents already in the local document cache only");
        options.addOption("solrserver", true, "URL for the SOLR instance (index-items)");
        options.addOption("solrlocalpath", true, "Absolute file path to a local SOLR database (will use an embedded SOLR instance) - make -solrserver option redundant");
        options.addOption("threads", true, "The number of concurrent threads to use");
    }

}
