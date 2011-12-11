package au.org.ala.bhl.actors;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.Routing;

public abstract class WorkerPool<T extends Actor> extends AbstractBHLActor {
    
    private int _workerCount;
    private ActorRef _router;
    
    public WorkerPool(int workerCount) {
        _workerCount = workerCount;
    }
    
    
    @Override
    public void preStart() {
        final ActorRef[] workers = new ActorRef[_workerCount];        
        for (int i = 0; i < _workerCount; i++) {
            workers[i] = Actors.actorOf(new UntypedActorFactory() {               
                public Actor create() {
                    return createWorker();
                }
            }).start();
        }
        
        _router = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {
                return new RoundRobinRouter(workers);
            }
        }).start();        
    }
    
    
    @Override
    public void postStop() {
    	_router.tell(new Routing.Broadcast(Actors.poisonPill()));
        _router.stop();
    }
        
    @Override
    public void onReceive(Object message) throws Exception {
        _router.tell(message);     
    }
        
    protected abstract T createWorker();


}
