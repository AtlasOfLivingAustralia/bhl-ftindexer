package au.org.ala.bhl.actors;


public class ItemRetriever extends WorkerPool<RetrieveItemWorker> {
    
    private String _docCachePath;

    public ItemRetriever(int workerCount, String docCachePath) {
        super(workerCount);
        _docCachePath = docCachePath;
    }

    @Override
    protected RetrieveItemWorker createWorker() {
        return new RetrieveItemWorker(_docCachePath);
    }

}
