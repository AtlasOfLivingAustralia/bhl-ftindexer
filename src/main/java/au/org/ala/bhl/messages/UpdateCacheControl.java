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
package au.org.ala.bhl.messages;

import au.org.ala.bhl.IndexingController;
import au.org.ala.bhl.ItemDescriptor;

/**
 * Message used to trigger a cache control update on a particular item
 * 
 * @author baird
 *
 */
public class UpdateCacheControl extends AbstractItemMessage {

    private final ItemDescriptor _item;

    public UpdateCacheControl(IndexingController controller, ItemDescriptor item) {
        super(controller);
        _item = item;
    }

    public ItemDescriptor getItem() {
        return _item;
    }

}
