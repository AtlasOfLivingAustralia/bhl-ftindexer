package au.org.ala.bhl.command;

import org.apache.commons.cli.Options;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.DocumentPaginator;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.PageHandler;
import au.org.ala.bhl.service.ItemSourceService;

@Command(name = "generate-pages")
public class GeneratePageXMLCommand extends AbstractCommand {

    public void execute(ItemSourceService service, IndexerOptions options) throws Exception {
        String itemId = "meteorology02brit";
        String filename = String.format("%s/%s.xml", options.getDocCachePath(), itemId);

        DocumentPaginator paginator = new DocumentPaginator();
        paginator.paginate(filename, new PageHandler() {
            public void handlePage(String pageId, String pageText) {
                emitPage(pageId, pageText);
            }
        });

    }

    private void emitPage(String pageId, String text) {
        System.out.println("PageID: " + pageId + " *********");
        System.out.println(text);
    }

    public void defineOptions(Options options) {
    }

}
