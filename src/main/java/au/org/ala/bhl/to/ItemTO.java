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
package au.org.ala.bhl.to;

import java.sql.Date;

import au.org.ala.bhl.MappingInfo;

/**
 * Simple transfer object that represents an item in the ItemService database
 * 
 * @author baird
 *
 */
public class ItemTO {

    private String primaryTitleId;
    private String itemId;
    private String internetArchiveId;
    private String title;
    private String volume;
    private String localCacheFile;
    private String status;
    private Date lastModified;
    private int pageCount;
    
    public String getPrimaryTitleId() {
        return primaryTitleId;
    }

    @MappingInfo(column="PrimaryTitleID")
    public void setPrimaryTitleId(String primaryTitleId) {
        this.primaryTitleId = primaryTitleId;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    @MappingInfo(column="ItemId")
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getInternetArchiveId() {
        return internetArchiveId;
    }

    @MappingInfo(column="IACode")
    public void setInternetArchiveId(String internetArchiveId) {
        this.internetArchiveId = internetArchiveId;
    }

    public String getTitle() {
        return title;
    }

    @MappingInfo(column="Title")
    public void setTitle(String title) {
        this.title = title;
    }

    public String getVolume() {
        return volume;
    }

    @MappingInfo(column="Volume")
    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getLocalCacheFile() {
        return localCacheFile;
    }

    @MappingInfo(column="LocalCacheFile")
    public void setLocalCacheFile(String localCacheFile) {
        this.localCacheFile = localCacheFile;
    }

    public String getStatus() {
        return status;
    }

    @MappingInfo(column="Status")
    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastModified() {
        return lastModified;
    }

    @MappingInfo(column="DateModified")
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
        
    public int getPageCount() {
        return pageCount;
    }
    
    @MappingInfo(column="PageCount")
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

}
