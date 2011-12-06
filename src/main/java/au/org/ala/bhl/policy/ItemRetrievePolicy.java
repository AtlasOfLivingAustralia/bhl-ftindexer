package au.org.ala.bhl.policy;

import au.org.ala.bhl.to.ItemTO;

public interface ItemRetrievePolicy {
    
    boolean shouldRetrieveItem(ItemTO item);

}
