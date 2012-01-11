package au.org.ala.bhl.service;

import java.io.File;

public interface CachedItemPageHandler {
	
	void startItem(String itemId);
	
	void onPage(String iaId, String pageId, File pageFile);
	
	void endItem(String itemId);

}
