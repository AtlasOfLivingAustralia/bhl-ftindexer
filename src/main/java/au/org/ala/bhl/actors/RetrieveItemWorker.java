package au.org.ala.bhl.actors;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;

import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.messages.RetrieveAndIndexItemText;
import au.org.ala.bhl.messages.RetrieveItemText;
import au.org.ala.bhl.service.DocumentCacheService;

public class RetrieveItemWorker extends AbstractBHLActor {
	
	private static String SEPARATOR = System.getProperty("file.separator");

    private final DocumentCacheService _docCache;

    public RetrieveItemWorker(String docCachePath) {
        _docCache = new DocumentCacheService(docCachePath);
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof RetrieveItemText) {
            RetrieveItemText indexMessage = (RetrieveItemText) message;
            ItemDescriptor item = indexMessage.getItem();
            final String iaId = item.getInternetArchiveId();
            String itemDir = _docCache.getItemDirectoryPath(iaId);
            String completeFilePath = String.format("%s%s.complete", itemDir, SEPARATOR);
            File completeFile = new File(completeFilePath);
            File f = new File(itemDir);
            if (f.exists() && completeFile.exists()) {
                if (message instanceof RetrieveAndIndexItemText) {
                    log("Reindexing existing item: %s", itemDir);
                    indexMessage.getController().indexItem(item, itemDir);
                } else {
                    log("File already exists. Skipping (%s)", itemDir);
                }

                return;
            }

            try {
                log("Retrieving missing or incomplete item %s (IA: %s)", item.getItemId(), item.getInternetArchiveId());               
                JsonNode node = webServiceCallJson(item.getItemMetaDataURL());
                if (node != null) {
                    File documentDir = new File(_docCache.getItemDirectoryPath(item.getInternetArchiveId()));
                    if (!documentDir.exists()) {
                        log("Creating directory: %s", documentDir.getAbsoluteFile());
                        documentDir.mkdir();
                    }

                    processPageMetaData(node, item);

                    getItemsService().setItemStatus(item.getItemId(), ItemStatus.FETCHED, 0);
                    FileUtils.writeStringToFile(completeFile, String.format("%s", Calendar.getInstance().getTime()));
                } else {
                    log("Failed to get item meta data from BHL-AU for item %s", item.getItemId());
                }

                getItemsService().setItemLocalPath(item.getItemId(), itemDir);
                
                if (message instanceof RetrieveAndIndexItemText) {
                    log("Scheduling retrieved item for indexing: %s", itemDir);
                    indexMessage.getController().indexItem(item, itemDir);
                }
                
            } catch (Exception ex) {
                log(ex.getMessage());
            }
        } else {
            throw new IllegalStateException("Invalid message: " + message);
        }
    }

    private boolean processPageMetaData(JsonNode root, ItemDescriptor item) throws IOException {
        JsonNode pagesNode = root.path("Result").path("Pages");
        if (pagesNode != null && pagesNode.isArray()) {
            int pageCount = 0;
            int skipCount = 0;
            for (int i = 0; i < pagesNode.size(); ++i) {
                JsonNode node = pagesNode.get(i);
                int pageId = node.get("PageID").getIntValue();
                String pagePath = String.format("%s%s%05d_%d.txt", _docCache.getItemDirectoryPath(item.getInternetArchiveId()), SEPARATOR, i, pageId);
                File pageFile = new File(pagePath);
                if (!pageFile.exists()) {
                    String ocrURL = node.get("OcrUrl").getTextValue();
                    if (StringUtils.isNotEmpty(ocrURL)) {
                        log("Retrieving page %d of %d (Page ID %d for item %s)", i+1, pagesNode.size(), pageId, item.getItemId());
                        String ocr = webGetText(ocrURL);
                        FileUtils.writeStringToFile(pageFile, ocr);
                        pageCount++;
                    } else {
                        log("OCR text is empty for item %s (IA: %s)", item.getItemId(), item.getInternetArchiveId());
                    }
                } else {
                    skipCount++;
                }

            }
            log("Item text retrieved for item %s (IA: %s) - %d pages of OCR retrieved, %d existing pages skipped.", item.getItemId(), item.getInternetArchiveId(), pageCount, skipCount);
            return true;
        } else {
            log("No pages found for item %s (IA: %s). Skipping.", item.getItemId(), item.getInternetArchiveId());
        }
        return false;
    }

}
