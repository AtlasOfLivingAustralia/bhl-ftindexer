package au.org.ala.bhl.actors;

import au.org.ala.bhl.IndexerOptions;

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
