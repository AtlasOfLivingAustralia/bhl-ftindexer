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

import org.apache.commons.cli.Options;

import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.service.ItemsService;

/**
 * Interface that describes a command-line application command
 * 
 * @author baird
 *
 */
public interface CommandLineCommand {
   
	/**
	 * Actually executes the command
	 * 
	 * @param service
	 * @param options
	 * @throws Exception
	 */
    void execute(final ItemsService service, final IndexerOptions options) throws Exception;
    
    /**
     * Allows the command to describe what options it expects
     * @param options
     */
    void defineOptions(final Options options);
    
}
