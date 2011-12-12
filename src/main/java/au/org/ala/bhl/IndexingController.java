package au.org.ala.bhl;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActorFactory;
import akka.routing.Routing;
import au.org.ala.bhl.actors.ItemIndexerPool;
import au.org.ala.bhl.actors.ItemRetrieverPool;
import au.org.ala.bhl.actors.UtilityActorPool;
import au.org.ala.bhl.messages.IndexText;
import au.org.ala.bhl.messages.RetrieveAndIndexItemText;
import au.org.ala.bhl.messages.RetrieveItemText;
import au.org.ala.bhl.messages.UpdateCacheControl;
import au.org.ala.bhl.service.LogService;

public class IndexingController {

    private ActorRef _retrieverPool;
    private ActorRef _indexerPool;
    private ActorRef _utilityPool;
    private IndexerOptions _options;

    public IndexingController(final IndexerOptions options) {        
        _options = options;        
        _retrieverPool = Actors.actorOf(new UntypedActorFactory() {
            public Actor create() {
                return new ItemRetrieverPool(options.getRetrieveThreadCount(), _options.getDocCachePath());
            }
        });
        
        _indexerPool = Actors.actorOf(new UntypedActorFactory() {
            public Actor create() {
                return new ItemIndexerPool(options.getIndexerThreadCount(), _options.getSolrServerURL());
            }
        });
        
        _utilityPool = Actors.actorOf(new UntypedActorFactory() {
			
			public Actor create() {
				return new UtilityActorPool(options);
			}
		});
                
        _retrieverPool.start();
        _indexerPool.start(); 
        _utilityPool.start();
    }

    public void retrieveItem(ItemDescriptor item) {
        _retrieverPool.tell(new RetrieveItemText(this, item), _retrieverPool);
    }
    
    public void retrieveAndIndexItem(ItemDescriptor item) {
        _retrieverPool.tell(new RetrieveAndIndexItemText(this, item), _retrieverPool);
    }
    
    public void indexItem(ItemDescriptor item, String filename) {
        _indexerPool.tell(new IndexText(this, item, filename), _indexerPool);
    }

    public void queueStopMessages() {
    	log("Adding stop messages to queues...");
    	
    	_retrieverPool.tell(new Routing.Broadcast(Actors.poisonPill()));
        _retrieverPool.tell(Actors.poisonPill());
        
        _indexerPool.tell(new Routing.Broadcast(Actors.poisonPill()));
        _indexerPool.tell(Actors.poisonPill());
        
        _utilityPool.tell(new Routing.Broadcast(Actors.poisonPill()));
        _utilityPool.tell(Actors.poisonPill());
    }
    
    public void updateCacheControl(ItemDescriptor item) {
    	_utilityPool.tell(new UpdateCacheControl(this, item));
    }
    
    protected void log(String format, Object... args) {
        LogService.log(this.getClass(), format, args);
    }

}
