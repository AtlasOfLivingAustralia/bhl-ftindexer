package au.org.ala.bhl.messages;

import au.org.ala.bhl.IndexingController;
import au.org.ala.bhl.ItemDescriptor;

public class RetrieveAndIndexItemText extends RetrieveItemText {

    public RetrieveAndIndexItemText(IndexingController controller, ItemDescriptor item) {
        super(controller, item);
    }

}
