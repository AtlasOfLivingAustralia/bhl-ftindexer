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
        }, createItemFilter(indexerOptions));
        _indexer.queueStopMessages();
        log("Jobs queued, Waiting...");
    }

    public void defineOptions(Options options) {
    }

}
