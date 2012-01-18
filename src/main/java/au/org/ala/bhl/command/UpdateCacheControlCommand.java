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
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

/**
 * For each item that exists in the doc cache, document item meta data is extracted from BHL and stored in the .metadata file (JSON)
 * 
 * @author baird
 *
 */
@Command(name = "update-cache-control")
public class UpdateCacheControlCommand extends AbstractCommand {

	public void execute(ItemsService service, IndexerOptions options) throws Exception {

		final IndexingController indexer = new IndexingController(options);

		service.forAllItems(new ItemTOHandler() {
			public void onItem(ItemTO item) {
				indexer.updateCacheControl(createItemDescriptor(item));
			}
		}, createItemFilter(options));
		indexer.queueStopMessages();
		log("Jobs queued, Waiting...");
	}

	public void defineOptions(Options options) {
	}

}
