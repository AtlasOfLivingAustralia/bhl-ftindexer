/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.bhl.actors;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.routing.Routing;

/**
 * Represents a pool of typed actors, to which jobs are allocated in a round robin fashion
 * 
 * @author baird
 *
 * @param <T>
 */
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
