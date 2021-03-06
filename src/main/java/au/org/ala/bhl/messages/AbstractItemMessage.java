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

import java.util.concurrent.atomic.AtomicInteger;

import au.org.ala.bhl.IndexingController;

/**
 * Base class for all AKKA messages 
 */
public class AbstractItemMessage {
	

	private static AtomicInteger _lastJobId = new AtomicInteger(0);

	/** A process wide unique integer for each message */
	private int _jobId = _lastJobId.incrementAndGet();
    
    private IndexingController _controller;
    
    protected AbstractItemMessage(IndexingController controller) {
        _controller = controller;        
    }
    
    public IndexingController getController() {
        return _controller;
    }
    
    public int getJobId() {
    	return _jobId;
    }
    
    public static int getLastJobId() {
    	return _lastJobId.get();
    }

}


