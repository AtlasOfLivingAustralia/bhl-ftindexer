package au.org.ala.bhl.actors;

import au.org.ala.bhl.messages.UpdateCacheControl;

public class UtilityWorker extends AbstractBHLActor {

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof UpdateCacheControl) {
			UpdateCacheControl msg = (UpdateCacheControl) message;
			log("Updating cache control for item: %s", msg.getItem().getInternetArchiveId());
		} else if (message instanceof String) {
			String msg = (String) message;
			System.err.println(msg);
		} else {
			throw new IllegalStateException("Invalid message: " + message);
		}
	}

	private void updateCacheControl(UpdateCacheControl msg) {
		// String itemDir = docCache.getItemDirectoryPath(item.getInternetArchiveId());
		// String completeFilePath = String.format("%s\\.complete", itemDir);
		// File completeFile = new File(completeFilePath);
		//
		// ItemDescriptor itemDesc = createItemDescriptor(item);
		//
		// if (completeFile.exists()) {
		//
		// }

	}

}
