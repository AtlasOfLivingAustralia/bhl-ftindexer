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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemFilter;
import au.org.ala.bhl.service.LogService;
import au.org.ala.bhl.to.ItemTO;

public abstract class AbstractCommand implements CommandLineCommand {

    protected void log(String format, Object... args) {
        LogService.log(this.getClass(), format, args);
    }
    
    protected ItemDescriptor createItemDescriptor(ItemTO item) {
        return new ItemDescriptor(item.getPrimaryTitleId(), item.getItemId(), item.getInternetArchiveId(), item.getTitle(), item.getVolume());
    }

    protected ItemFilter createItemFilter(IndexerOptions options) {
    	if (!StringUtils.isEmpty(options.getItemFilter())) {
    		return new InternetArchiveIDRegexFilter(options.getItemFilter());
    	}
    	return null;
    }
    
    class InternetArchiveIDRegexFilter implements ItemFilter {
    	
    	private String _filter;
    	private Pattern _regex;
    	
    	public InternetArchiveIDRegexFilter(String filter) {
    		_filter = filter;
    		_regex = Pattern.compile(_filter);
    	}

		public boolean accept(ItemTO item) {
			Matcher m = _regex.matcher(item.getInternetArchiveId());
			if (m.find()) {
				return true;
			}
			return false;
		}
    	
    }
}
