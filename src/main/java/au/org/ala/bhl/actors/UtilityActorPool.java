package au.org.ala.bhl.actors;

import au.org.ala.bhl.IndexerOptions;

public class UtilityActorPool extends WorkerPool<UtilityWorker> {
	
	private IndexerOptions _options;

	public UtilityActorPool(IndexerOptions options) {
		super(options.getUtilityThreadCount());
		_options = options;
	}

	@Override
	protected UtilityWorker createWorker() {
		return new UtilityWorker(_options);
	}

}
