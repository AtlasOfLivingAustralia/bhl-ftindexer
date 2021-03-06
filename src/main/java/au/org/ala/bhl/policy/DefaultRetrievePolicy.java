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
package au.org.ala.bhl.policy;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.to.ItemTO;

/**
 * policy object used to determine if an items text should be retrieved (or re-retrieved).
 * 
 * @author baird
 *
 */
public class DefaultRetrievePolicy implements ItemRetrievePolicy {

    public boolean shouldRetrieveItem(ItemTO item) {

        if (!StringUtils.isEmpty(item.getLocalCacheFile())) {
            File cachedFile = new File(item.getLocalCacheFile());
            if (!cachedFile.exists()) {
                return true;
            }
        }

        if (StringUtils.isEmpty(item.getStatus())) {
            return true;
        }

        return false;
    }

}
