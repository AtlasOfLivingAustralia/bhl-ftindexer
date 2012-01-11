package au.org.ala.bhl;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActorFactory;
import akka.routing.Routing;
import au.org.ala.bhl.actors.JobActorPool;
import au.org.ala.bhl.messages.IndexText;
import au.org.ala.bhl.messages.RetrieveAndIndexItemText;
import au.org.ala.bhl.messages.RetrieveItemText;
import au.org.ala.bhl.messages.UpdateCacheControl;
import au.org.ala.bhl.service.LogService;

public class IndexingController {

    private ActorRef _jobActorPool;
    protected IndexerOptions _options;

    public IndexingController(final IndexerOptions options) {        
        _options = options;                
        _jobActorPool = Actors.actorOf(new UntypedActorFactory() {
			
			public Actor create() {
				return new JobActorPool(options);
			}
		});                
        _jobActorPool.start();
    }

    public void retrieveItem(ItemDescriptor item) {
        _jobActorPool.tell(new RetrieveItemText(this, item), _jobActorPool);
    }
    
    public void retrieveAndIndexItem(ItemDescriptor item) {
    	_jobActorPool.tell(new RetrieveAndIndexItemText(this, item), _jobActorPool);
    }
    
    public void indexItem(ItemDescriptor item, String filename) {
    	_jobActorPool.tell(new IndexText(this, item, filename), _jobActorPool);
    }

    public void queueStopMessages() {
    	log("Adding stop messages to queue...");    	       
        _jobActorPool.tell(new Routing.Broadcast(Actors.poisonPill()));
        _jobActorPool.tell(Actors.poisonPill());
    }
    
    public void updateCacheControl(ItemDescriptor item) {
    	_jobActorPool.tell(new UpdateCacheControl(this, item));
    }
    
    protected void log(String format, Object... args) {
        LogService.log(this.getClass(), format, args);
    }

}
