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
package au.org.ala.bhl.actors;

import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.messages.AbstractItemMessage;
import au.org.ala.bhl.messages.IndexText;
import au.org.ala.bhl.messages.RetrieveAndIndexItemText;
import au.org.ala.bhl.messages.RetrieveItemText;
import au.org.ala.bhl.messages.Shutdown;
import au.org.ala.bhl.messages.UpdateCacheControl;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.IndexingService;

/**
 * Actor resposible for executing the various tasks that require concurrency. Tasks are identified by their message type, and each message should be unique and
 * contain all necessary information required by the task so that thread safety can be preserved.
 *  
 * @author baird
 *
 */
public class JobWorker extends AbstractBHLActor {

	private DocumentCacheService _docCache;
	private IndexingService _indexingService;
	protected IndexerOptions _options;

	/**
	 * Ctor
	 * @param options
	 */
	public JobWorker(IndexerOptions options) {
		_options = options;
		_docCache = new DocumentCacheService(options.getDocCachePath());
		_indexingService = new IndexingService(options.getSolrServerURL(), _docCache);
	}

	/**
	 * Simply switches on message type and delegates to a method that understands each message type
	 */
	@Override
	public void onReceive(Object message) throws Exception {
		
		if (message instanceof AbstractItemMessage) {
			AbstractItemMessage msg = (AbstractItemMessage) message;
			log("Processing job id: %d (of %d)", msg.getJobId(), AbstractItemMessage.getLastJobId());
		}
		
		if (message instanceof UpdateCacheControl) {
			UpdateCacheControl msg = (UpdateCacheControl) message;
			updateCacheControl(msg);
		} else if (message instanceof RetrieveItemText) {
			RetrieveItemText msg = (RetrieveItemText) message;
			retrieveItem(msg);
		} else if (message instanceof IndexText) {
			IndexText msg = (IndexText) message;
			indexItem(msg);
		} else if (message instanceof Shutdown) {			
			_indexingService.shutdown();
		} else {
			throw new IllegalStateException("Invalid message: " + message);
		}
	}

	/**
	 * Index an item
	 * @param msg
	 */
	private void indexItem(IndexText msg) {
		_indexingService.indexItem(msg.getItem());
	}

	/** 
	 * Retrieve ocr text for an item and store in the document cache
	 * @param msg
	 */
	private void retrieveItem(RetrieveItemText msg) {		
		_docCache.retrieveItem(msg.getItem(), true);
		if (msg instanceof RetrieveAndIndexItemText) {
			_indexingService.indexItem(msg.getItem());
		}
	}

	/**
	 * Update the cache control block for an item
	 * 
	 * @param msg
	 */
	private void updateCacheControl(UpdateCacheControl msg) {
		ItemDescriptor item = msg.getItem();
		_docCache.createCacheControl(item, _options.getForce());
	}

}
