package au.org.ala.bhl;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActorFactory;
import au.org.ala.bhl.actors.IndexText;
import au.org.ala.bhl.actors.ItemIndexer;
import au.org.ala.bhl.actors.RetrieveAndIndexItemText;
import au.org.ala.bhl.actors.RetrieveItemText;
import au.org.ala.bhl.actors.ItemRetriever;

public class IndexingController {

    private ActorRef _retriever;
    private ActorRef _indexer;
    private IndexerOptions _options;

    public IndexingController(final IndexerOptions options) {        
        _options = options;        
        _retriever = Actors.actorOf(new UntypedActorFactory() {
            public Actor create() {
                return new ItemRetriever(options.getRetrieveThreadCount(), _options.getDocCachePath());
            }
        });
        
        _indexer = Actors.actorOf(new UntypedActorFactory() {
            public Actor create() {
                return new ItemIndexer(options.getIndexerThreadCount(), _options.getSolrServerURL());
            }
        });
                
        _retriever.start();
        _indexer.start();       
    }

    public void retrieveItem(ItemDescriptor item) {
        _retriever.tell(new RetrieveItemText(this, item), _retriever);
    }
    
    public void retrieveAndIndexItem(ItemDescriptor item) {
        _retriever.tell(new RetrieveAndIndexItemText(this, item), _retriever);
    }
    
    public void indexItem(ItemDescriptor item, String filename) {
        _indexer.tell(new IndexText(this, item, filename), _indexer);
    }

    public void stop() {
        _retriever.tell(Actors.poisonPill());
        _indexer.tell(Actors.poisonPill());       
    }

}
