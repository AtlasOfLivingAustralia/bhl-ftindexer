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
package au.org.ala.bhl.actors;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;

import akka.actor.UntypedActor;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.service.LogService;
import au.org.ala.bhl.service.WebServiceHelper;

/**
 * Base class for all actors. Provides access to an ItemsService as well as adding from convenience helper functions
 *  
 * @author baird
 *
 */
public abstract class AbstractBHLActor extends UntypedActor {

	private ItemsService _service;

	/**
	 * Logs a message via the logging service
	 * 
	 * @param format
	 * @param args
	 */
	protected void log(String format, Object... args) {
		LogService.log(this.getClass(), format, args);
	}

	/*
	 * get an instance of the items service
	 */
	protected ItemsService getItemsService() {
		if (_service == null) {
			_service = new ItemsService();
		}
		return _service;
	}

	/**
	 * Make a web service call that returns JSON
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	protected JsonNode webServiceCallJson(String uri) throws IOException {
		return WebServiceHelper.getJSON(uri);
	}

	/**
	 * Retrieve the document text addressed by a URL.
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	protected String webGetText(String uri) throws IOException {
		return WebServiceHelper.getText(uri);
	}

}
