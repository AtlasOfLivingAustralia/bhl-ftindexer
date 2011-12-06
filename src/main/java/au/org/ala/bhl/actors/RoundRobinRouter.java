package au.org.ala.bhl.actors;

import java.util.Arrays;

import akka.actor.ActorRef;
import akka.routing.CyclicIterator;
import akka.routing.InfiniteIterator;
import akka.routing.UntypedLoadBalancer;

public class RoundRobinRouter extends UntypedLoadBalancer {

    private final InfiniteIterator<ActorRef> _workers;        

    public RoundRobinRouter(ActorRef[] workers) {
        _workers = new CyclicIterator<ActorRef>(Arrays.asList(workers));
    }

    @Override
    public InfiniteIterator<ActorRef> seq() {
        return _workers;
    }

}
