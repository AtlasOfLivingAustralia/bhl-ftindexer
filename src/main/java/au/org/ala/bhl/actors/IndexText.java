package au.org.ala.bhl.actors;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.IndexingController;

public class IndexText extends AbstractItemMessage {
    
    private final ItemDescriptor _item;
    private final String _itemPath;
    
    public IndexText(IndexingController controller, ItemDescriptor item, String itemPath) {
        super(controller);
        _item = item;
        _itemPath = itemPath;
    }
    
    public ItemDescriptor getItem() {
        return _item;
    }
    
    public String getItemPath() {
        return _itemPath;
    }

}
