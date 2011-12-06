package au.org.ala.bhl.command;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.service.LogService;
import au.org.ala.bhl.to.ItemTO;

public abstract class AbstractCommand implements CommandLineCommand {

    protected void log(String format, Object... args) {
        LogService.log(this.getClass(), format, args);
    }
    
    protected ItemDescriptor createItemDescriptor(ItemTO item) {
        return new ItemDescriptor(item.getPrimaryTitleId(), item.getItemId(), item.getInternetArchiveId(), item.getTitle(), item.getVolume());
    }

}
