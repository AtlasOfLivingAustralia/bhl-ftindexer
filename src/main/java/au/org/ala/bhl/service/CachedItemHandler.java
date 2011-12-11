package au.org.ala.bhl.service;

import java.io.File;

public interface CachedItemHandler {
	void onItem(File itemDir);
	void onProgress(int countComplete, int percentComplete);
}
