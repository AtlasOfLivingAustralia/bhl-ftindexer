package au.org.ala.bhl.actors;

import au.org.ala.bhl.IndexingController;

public class AbstractItemMessage {
    
    private IndexingController _controller;
    
    protected AbstractItemMessage(IndexingController controller) {
        _controller = controller;        
    }
    
    public IndexingController getController() {
        return _controller;
    }

}


