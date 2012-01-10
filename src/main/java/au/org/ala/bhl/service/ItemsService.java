package au.org.ala.bhl.service;

import java.io.File;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.ReflectiveMapper;
import au.org.ala.bhl.to.ItemTO;

public class ItemsService extends H2Service {

    public ItemsService() {
        super("/data/bhl-ftindex/itemsource");
    }

    public void addItem(ItemDescriptor item) {
        update("MERGE INTO Items (PrimaryTitleID, ItemID, IACode, Title, Volume) VALUES (?, ?, ?, ?, ?)", item.getPrimaryTitleId(), item.getItemId(),item.getInternetArchiveId(), item.getTitle(),item.getVolume());
    }
    
    public void updateCachedFileInfo(String itemId, File f) {
        if (f != null && f.exists()) {
            update("UPDATE ITEMS SET LocalCacheFile = ?, DateModified = ?, Status = ? WHERE ItemID = ?", f.getAbsolutePath(), new Date(f.lastModified()), ItemStatus.FETCHED, itemId);
        }
    }
    
    public void clearAllItems() {
        nonQuery("DELETE FROM Items");
    }
    
    public void forAllItems(final ItemTOHandler handler) {
        
        final ReflectiveMapper<ItemTO> mapper = new ReflectiveMapper<ItemTO>();
        
        queryForEach("SELECT ItemID, PrimaryTitleID, IACode, Title, Volume, LocalCacheFile, Status, DateModified, PageCount FROM Items", new ResultSetHandler() {            
            public void onRow(ResultSet rs) throws SQLException {                
                ItemTO item = new ItemTO();                
                mapper.map(rs, item);                
                if (handler != null) {
                    handler.onItem(item);
                }
            }
        });        
    }
    
    public void setItemStatus(String itemId, String status, int pageCount) {
        update("UPDATE ITEMS SET Status = ?, DateModified = ?, PageCount = ? WHERE ItemID = ?", status, now(), pageCount, itemId);
    }

    @Override
    protected void init() {
        nonQuery("CREATE TABLE IF NOT EXISTS Items (PrimaryTitleID VARCHAR(50), ItemID VARCHAR(50) PRIMARY KEY, IACode VARCHAR(50), Title VARCHAR(1024), Volume VARCHAR(255), LocalCacheFile VARCHAR(255), PageCount INT DEFAULT 0, Status VARCHAR(50), DateModified DATE)");
        nonQuery("CREATE UNIQUE INDEX IF NOT EXISTS IACodeIndex ON Items (IACode)");
    }

    public void setItemLocalPath(String itemId, String filename) {
        update("UPDATE ITEMS SET LocalCacheFile = ?, DateModified = ? WHERE ItemID = ?", filename, now(), itemId);        
    }
       
}
