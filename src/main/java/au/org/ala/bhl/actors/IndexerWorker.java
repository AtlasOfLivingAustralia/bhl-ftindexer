package au.org.ala.bhl.actors;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.messages.IndexText;

public class IndexerWorker extends AbstractBHLActor {

    private final String _serverURL;
    private SolrServer _server;

    private static Pattern PAGE_FILE_REGEX = Pattern.compile("^(\\d{5})_(\\d+).txt$");

    public IndexerWorker(String serverUrl) {
        _serverURL = serverUrl;
        _server = getSolrServer();
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
            doc.addField("name", item.getName(), 1.0f);
            doc.addField("text", pageText);
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
