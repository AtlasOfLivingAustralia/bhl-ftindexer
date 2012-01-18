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
package au.org.ala.bhl.command;

import java.io.File;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

/**
 * This command updates the database record for each item depending whether it exists in the document cache. If the item exists, and it has not
 * already been indexed, its status is set to  Fetched, and its document path is updated. If the item does not exist its status and local path is cleared.
 * @author baird
 *
 */
@Command(name = "maintain-cache")
public class MaintainCacheCommand extends AbstractCommand {
	
    public void execute(final ItemsService service, final IndexerOptions options) throws Exception {
        
        final DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());

        service.forAllItems(new ItemTOHandler() {

            public void onItem(ItemTO item) {                
                File f = new File(docCache.getItemDirectoryPath(item));
                log("Updating local file path for item %s (%s)", item.getInternetArchiveId(), f.getAbsolutePath());
                if (f.exists()) {
                    service.updateCachedFileInfo(item.getItemId(), f);
                    if (StringUtils.isNotEmpty(item.getStatus()) && !item.getStatus().equals(ItemStatus.INDEXED)) {
                        service.setItemStatus(item.getItemId(), ItemStatus.FETCHED, 0);
                    }
                } else {
                    service.setItemLocalPath(item.getItemId(), "");
                    service.setItemStatus(item.getItemId(), null, 0);
                }
            }
        }, createItemFilter(options));
    }

    public void defineOptions(Options options) {
    }

}
