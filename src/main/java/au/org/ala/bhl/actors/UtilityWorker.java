package au.org.ala.bhl.actors;

import java.io.File;

import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.messages.UpdateCacheControl;
import au.org.ala.bhl.service.DocumentCacheService;

public class UtilityWorker extends AbstractBHLActor {

	private DocumentCacheService _docCache;

	public UtilityWorker(IndexerOptions options) {
		_docCache = new DocumentCacheService(options.getDocCachePath());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof UpdateCacheControl) {
			UpdateCacheControl msg = (UpdateCacheControl) message;
			updateCacheControl(msg);		
		} else {
			throw new IllegalStateException("Invalid message: " + message);
		}
	}

	private void updateCacheControl(UpdateCacheControl msg) {
		ItemDescriptor item = msg.getItem();
		_docCache.createCacheControl(item);
	}

}
