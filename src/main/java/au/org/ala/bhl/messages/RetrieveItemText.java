package au.org.ala.bhl.messages;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.IndexingController;

public class RetrieveItemText extends AbstractItemMessage {

    private final ItemDescriptor _item;

    public RetrieveItemText(IndexingController controller, ItemDescriptor item) {
        super(controller);
        _item = item;
    }

    public ItemDescriptor getItem() {
        return _item;
    }

}
