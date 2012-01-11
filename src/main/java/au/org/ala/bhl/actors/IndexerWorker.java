package au.org.ala.bhl.actors;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.plaf.ListUI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.LanguageScore;
import au.org.ala.bhl.TaxonGrab;
import au.org.ala.bhl.WordLists;
import au.org.ala.bhl.messages.IndexText;
import au.org.ala.bhl.service.CacheControlBlock;
import au.org.ala.bhl.service.DocumentCacheService;

public class IndexerWorker extends AbstractBHLActor {

    private final String _serverURL;
    private SolrServer _server;
    private TaxonGrab _taxonGrab;    
    private DocumentCacheService _docCache;

    private static Pattern PAGE_FILE_REGEX = Pattern.compile("^(\\d{5})_(\\d+).txt$");

    public IndexerWorker(String serverUrl, String cacheDir) {
        _serverURL = serverUrl;
        _server = getSolrServer();
        _taxonGrab = new TaxonGrab();        
        _docCache = new DocumentCacheService(cacheDir);
    }

    private SolrServer getSolrServer() {
        try {
            return new CommonsHttpSolrServer(_serverURL);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof IndexText) {
            IndexText msg = (IndexText) message;
            log("Indexing pages %s for item %s", msg.getItemPath(), msg.getItem().getItemId());
            final ItemDescriptor item = msg.getItem();

            File itemPath = new File(msg.getItemPath());
            if (itemPath.exists() && itemPath.isDirectory()) {
                File[] pageFiles = itemPath.listFiles();
                int pageCount = 0;
                for (File pageFile : pageFiles) {
                    Matcher m = PAGE_FILE_REGEX.matcher(pageFile.getName());
                    if (m.matches()) {
                        String pageId = m.group(2);
                        String pageText = FileUtils.readFileToString(pageFile);
                        indexPage(item, pageId, pageText);
                        pageCount++;
                    }
                }
                if (pageCount > 0) {
                    _server.commit();
                    getItemsService().setItemStatus(msg.getItem().getItemId(), ItemStatus.INDEXED, pageCount);
                    log("%d pages indexed for item: %s", pageCount, item.getItemId());
                } else {
                    log("Ignoring empty item (no pages): %s", item.getItemId());
                }

            }

        }
    }

    private void indexPage(ItemDescriptor item, String pageId, String pageText) {
        if (!StringUtils.isEmpty(pageText)) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", pageId, 1.0f);
            doc.addField("name", item.getTitle(), 1.0f);
            doc.addField("text", pageText);
            doc.addField("internetArchiveId", item.getInternetArchiveId());
            doc.addField("itemId", item.getItemId());
            doc.addField("pageId", pageId, 1.0f);
            
//            String language = "english";
//            
//            CacheControlBlock ccb = _docCache.getCacheControl(item.getInternetArchiveId()); 
//            if (ccb != null && !StringUtils.isEmpty(ccb.Language)) {
//            	language = ccb.Language;            
//            }
//
//			LanguageScore score = WordLists.detectLanguage(pageText, language);
//			String lang = language;
//			if (score != null &&  ! StringUtils.equalsIgnoreCase(score.getName(), language) && score.getScore() > .75) {
//				log("Page %s - %s language detected as %s (scored %g) - This conflicts with meta data language of %s", item.getItemId(), pageId, score.getName(), score.getScore(), language);
//				lang = score.getName();
//			}
//            
//            List<String> names = _taxonGrab.findNames(pageText, lang);
//            if (names.size() > 0) {
//            	String namesStr = StringUtils.join(names, ",");
//            	doc.addField("taxonNames", namesStr);
//            	log("Names detected in page %s (%s) : %s", pageId, item.getInternetArchiveId(), namesStr);
//            }
            
            try {
                _server.add(doc);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    // private class PageIndexer implements PageHandler {
    //
    // private int _pageCount;
    // private ItemDescriptor _item;
    //
    // public PageIndexer(ItemDescriptor item) {
    // _item = item;
    // }
    //
    // public void handlePage(String pageId, String pageText) {
    // _pageCount++;
    // if (!StringUtils.isEmpty(pageText)) {
    // SolrInputDocument doc = new SolrInputDocument();
    // doc.addField("id", pageId, 1.0f);
    // doc.addField("name", _item.getName(), 1.0f);
    // doc.addField("text", pageText);
    // try {
    // _server.add(doc);
    // } catch (Exception ex) {
    // throw new RuntimeException(ex);
    // }
    // }
    // }
    //
    // public int getPageCount() {
    // return _pageCount;
    // }
    //
    // }

}
