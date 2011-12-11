package au.org.ala.bhl.service;

import java.io.File;

public interface CachedItemPageHandler {
	
	void onPage(String itemId, String pageId, File pageFile);

}
