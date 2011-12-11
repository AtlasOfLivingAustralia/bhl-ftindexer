package au.org.ala.bhl;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.PoisonPill;
import akka.actor.UntypedActorFactory;
import akka.routing.Routing;
import au.org.ala.bhl.actors.UtilityActorPool;
import au.org.ala.bhl.service.LogService;

public class AKKATest {

	public static void main(String[] args) {

		log("Starting...");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				log("Runtime shutting down.");
			}
		});

		ActorRef pool = Actors.actorOf(new UntypedActorFactory() {
			public Actor create() {
				return new UtilityActorPool(1);
			}
		});
		
		pool.start();
		
		
		pool.tell("asd");
		
		pool.tell(new Routing.Broadcast(Actors.poisonPill()));
		pool.tell(Actors.poisonPill());

		log("Stopping");
	}

	protected static void log(String format, Object... args) {
		LogService.log(AKKATest.class, format, args);
	}

}
