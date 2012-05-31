/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.bhl.service;

import java.io.File;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemFilter;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.ReflectiveMapper;
import au.org.ala.bhl.to.ItemTO;

/**
 * Service class the represents the local status database for items held in the BHL
 * 
 * @author baird
 *
 */
public class ItemsService extends H2Service {

	/**
	 * CTOR
	 */
    public ItemsService() {
        super("itemsource"); // root name of the H2 database file(s)
    }

    /**
     * Inserts or Updates an items details into the database
     * @param item
     */
    public void addItem(ItemDescriptor item) {
        update("MERGE INTO Items (PrimaryTitleID, ItemID, IACode, Title, Volume) VALUES (?, ?, ?, ?, ?)", item.getPrimaryTitleId(), item.getItemId(),item.getInternetArchiveId(), item.getTitle(),item.getVolume());
    }
    
    /**
     * Updates cache data for a particular item
     * @param itemId
     * @param f
     */
    public void updateCachedFileInfo(String itemId, File f) {
        if (f != null && f.exists()) {
            update("UPDATE ITEMS SET LocalCacheFile = ?, DateModified = ?, Status = ? WHERE ItemID = ?", f.getAbsolutePath(), new Date(f.lastModified()), ItemStatus.FETCHED, itemId);
        }
    }
    
    /**
     * Removes all items from the database
     */
    public void clearAllItems() {
        nonQuery("DELETE FROM Items");
    }

    /**
     * Iterates over every item in the database, and allows a handler to process each item. items can be filtered by an optional filter
     * @param handler
     * @param filter - may be null
     */
    public void forAllItems(final ItemTOHandler handler, final ItemFilter filter) {
        
        final ReflectiveMapper<ItemTO> mapper = new ReflectiveMapper<ItemTO>();
        
        queryForEach("SELECT ItemID, PrimaryTitleID, IACode, Title, Volume, LocalCacheFile, Status, DateModified, PageCount FROM Items", new ResultSetHandler() {            
            public void onRow(ResultSet rs) throws SQLException {                
                ItemTO item = new ItemTO();                
                mapper.map(rs, item);                
                if (filter == null || filter.accept(item)) {
	                if (handler != null) {
	                    handler.onItem(item);
	                }
                }
            }
        });        
    }
    
    /**
     * Update the status field of a particular item
     * @param itemId
     * @param status
     * @param pageCount
     */
    public void setItemStatus(String itemId, String status, int pageCount) {
        update("UPDATE ITEMS SET Status = ?, DateModified = ?, PageCount = ? WHERE ItemID = ?", status, now(), pageCount, itemId);
    }
    
    /**
     * Update the status field of a particular item
     * @param itemId
     * @param status
     * @param pageCount
     */
    public void setItemStatus(String itemId, String status) {
        update("UPDATE ITEMS SET Status = ?, DateModified = ? WHERE ItemID = ?", status, now(), itemId);
    }
    

    /**
     * Creates the tables should they not already exist
     */
    @Override
    protected void init() {
        nonQuery("CREATE TABLE IF NOT EXISTS Items (PrimaryTitleID VARCHAR(50), ItemID VARCHAR(50) PRIMARY KEY, IACode VARCHAR(50), Title VARCHAR(1024), Volume VARCHAR(255), LocalCacheFile VARCHAR(255), PageCount INT DEFAULT 0, Status VARCHAR(50), DateModified DATE)");
        nonQuery("CREATE UNIQUE INDEX IF NOT EXISTS IACodeIndex ON Items (IACode)");
    }

    /**
     * Update the local file path for a particular item
     * 
     * @param itemId
     * @param filename
     */
    public void setItemLocalPath(String itemId, String filename) {
        update("UPDATE ITEMS SET LocalCacheFile = ?, DateModified = ? WHERE ItemID = ?", filename, now(), itemId);        
    }
       
}
