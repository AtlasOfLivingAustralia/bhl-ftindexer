package au.org.ala.bhl.messages;

import au.org.ala.bhl.IndexingController;
import au.org.ala.bhl.ItemDescriptor;

public class UpdateCacheControl extends AbstractItemMessage {

    private final ItemDescriptor _item;

    public UpdateCacheControl(IndexingController controller, ItemDescriptor item) {
        super(controller);
        _item = item;
    }

    public ItemDescriptor getItem() {
        return _item;
    }

}
