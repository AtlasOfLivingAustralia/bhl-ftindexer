package au.org.ala.bhl;

import au.org.ala.bhl.to.ItemTO;

public interface ItemFilter {
	
	boolean accept(ItemTO item);

}
