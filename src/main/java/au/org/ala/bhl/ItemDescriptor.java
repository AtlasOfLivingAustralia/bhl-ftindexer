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
package au.org.ala.bhl;

import org.apache.commons.lang.StringUtils;

public class ItemDescriptor {
    
    private final static String BHL_APIKEY = "fc123b64-9980-4891-86fb-bd9f70c1d237";

    private String _primaryTitleId;
    private String _itemId;
    private String _internetArchiveId;
    private String _title;
    private String _volume;

    public ItemDescriptor(String primaryTitleId, String itemId, String internetArchiveId, String title, String volume) {
        _primaryTitleId = primaryTitleId;
        _itemId = itemId;
        _internetArchiveId = internetArchiveId;
        _title = title;
        _volume = volume;

    }

    public String getPrimaryTitleId() {
        return _primaryTitleId;
    }

    public String getItemId() {
        return _itemId;
    }

    public String getInternetArchiveId() {
        return _internetArchiveId;
    }

    public String getName() {
        String name = _title;
        if (!StringUtils.isEmpty(_volume) && !_volume.equals("NULL")) {
            name += " " + _volume;
        }
        return name;
    }

    public String getTitle() {
        return _title;
    }

    public String getVolume() {
        return _volume;
    }

//    public String getItemTextURL() {
//        return String.format("http://www.archive.org/download/%s/%s_djvu.xml", _internetArchiveId, _internetArchiveId);
//    }
    
    public String getItemMetaDataURL() {
        return String.format("http://bhl.ala.org.au/api/rest?op=GetItemMetadata&itemid=%s&pages=t&apikey=%s&format=json", _itemId, BHL_APIKEY);
    }

}
