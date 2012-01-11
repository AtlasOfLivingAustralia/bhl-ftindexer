package au.org.ala.bhl.actors;


public class ItemIndexerPool extends WorkerPool<IndexerWorker>  {
    
    private String _serverUrl;
    private String _cacheDir;
    
    public ItemIndexerPool(int workerCount, String serverUrl, String cacheDir) {
        super(workerCount);
        _serverUrl = serverUrl;
        _cacheDir = cacheDir;
    }

    @Override
    protected IndexerWorker createWorker() {
        return new IndexerWorker(_serverUrl, _cacheDir);
    }

}
