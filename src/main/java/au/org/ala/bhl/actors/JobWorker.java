package au.org.ala.bhl.actors;

import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.messages.AbstractItemMessage;
import au.org.ala.bhl.messages.IndexText;
import au.org.ala.bhl.messages.RetrieveAndIndexItemText;
import au.org.ala.bhl.messages.RetrieveItemText;
import au.org.ala.bhl.messages.UpdateCacheControl;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.IndexingService;

public class JobWorker extends AbstractBHLActor {

	private DocumentCacheService _docCache;
	private IndexingService _indexingService;
	protected IndexerOptions _options;

	public JobWorker(IndexerOptions options) {
		_options = options;
		_docCache = new DocumentCacheService(options.getDocCachePath());
		_indexingService = new IndexingService(options.getSolrServerURL(), _docCache);
	}

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
		} else {
			throw new IllegalStateException("Invalid message: " + message);
		}
	}

	private void indexItem(IndexText msg) {
		_indexingService.indexItem(msg.getItem());
	}

	private void retrieveItem(RetrieveItemText msg) {		
		_docCache.retrieveItem(msg.getItem(), true);
		if (msg instanceof RetrieveAndIndexItemText) {
			_indexingService.indexItem(msg.getItem());
		}
	}

	private void updateCacheControl(UpdateCacheControl msg) {
		ItemDescriptor item = msg.getItem();
		_docCache.createCacheControl(item);
	}

}
