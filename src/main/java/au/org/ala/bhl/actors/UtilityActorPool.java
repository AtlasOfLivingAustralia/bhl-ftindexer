package au.org.ala.bhl.actors;

public class UtilityActorPool extends WorkerPool<UtilityWorker> {

	public UtilityActorPool(int workerCount) {
		super(workerCount);
	}

	@Override
	protected UtilityWorker createWorker() {
		return new UtilityWorker();
	}

}
