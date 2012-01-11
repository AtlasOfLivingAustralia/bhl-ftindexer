package au.org.ala.bhl.messages;

import java.util.concurrent.atomic.AtomicInteger;

import au.org.ala.bhl.IndexingController;

public class AbstractItemMessage {
	
	private static AtomicInteger _lastJobId = new AtomicInteger(0);
	
	private int _jobId = _lastJobId.incrementAndGet();
    
    private IndexingController _controller;
    
    
    protected AbstractItemMessage(IndexingController controller) {
        _controller = controller;        
    }
    
    public IndexingController getController() {
        return _controller;
    }
    
    public int getJobId() {
    	return _jobId;
    }
    
    public static int getLastJobId() {
    	return _lastJobId.get();
    }

}


