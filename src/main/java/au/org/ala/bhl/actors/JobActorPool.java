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

import au.org.ala.bhl.IndexerOptions;

/**
 * Represents a pool of JobWorker actors. Jobs are routed in a round robin fashion amongst all instances in the pool.
 * The pool size is determined by the thread count option 
 * 
 * @author baird
 *
 */
public class JobActorPool extends WorkerPool<JobWorker> {
	
	private IndexerOptions _options;

	public JobActorPool(IndexerOptions options) {
		super(options.getThreadCount());
		_options = options;
	}

	@Override
	protected JobWorker createWorker() {
		return new JobWorker(_options);
	}

}
