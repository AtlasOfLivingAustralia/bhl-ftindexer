package au.org.ala.bhl.actors;

import au.org.ala.bhl.ItemDescriptor;

public class RetrieveItemResult {

    private final boolean _successful;
    private ItemDescriptor _item;
    private String _filename;

    public RetrieveItemResult(ItemDescriptor item, boolean successful, String filename) {
        _successful = successful;
        _item = item;
        _filename = filename;
    }

    public boolean isSuccessful() {
        return _successful;
    }

    public ItemDescriptor getItem() {
        return _item;
    }
    
    public String getFilename() {
        return _filename;
    }

}
