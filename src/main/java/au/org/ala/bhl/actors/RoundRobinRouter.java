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

import java.util.Arrays;

import akka.actor.ActorRef;
import akka.routing.CyclicIterator;
import akka.routing.InfiniteIterator;
import akka.routing.UntypedLoadBalancer;

/**
 * Actor load balancer that routes jobs in a round robin fashion (CyclicIterator)
 * 
 * @author baird
 *
 */
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
