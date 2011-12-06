package au.org.ala.bhl.actors;


public class ItemIndexer extends WorkerPool<IndexerWorker>  {
    
    private String _serverUrl;
    
    public ItemIndexer(int workerCount, String serverUrl) {
        super(workerCount);
        _serverUrl = serverUrl;
    }

    @Override
    protected IndexerWorker createWorker() {
        return new IndexerWorker(_serverUrl);
    }

}
