package au.org.ala.bhl.actors;


public class ItemIndexerPool extends WorkerPool<IndexerWorker>  {
    
    private String _serverUrl;
    
    public ItemIndexerPool(int workerCount, String serverUrl) {
        super(workerCount);
        _serverUrl = serverUrl;
    }

    @Override
    protected IndexerWorker createWorker() {
        return new IndexerWorker(_serverUrl);
    }

}
