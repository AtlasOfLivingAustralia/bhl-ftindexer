package au.org.ala.bhl.command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.LanguageScore;
import au.org.ala.bhl.TaxonGrab;
import au.org.ala.bhl.WordLists;
import au.org.ala.bhl.service.CacheControlBlock;
import au.org.ala.bhl.service.CachedItemPageHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;

@Command(name = "extract-names")
public class ExtractNamesCommand extends AbstractCommand {
	
	

	public void execute(final ItemsService service, final IndexerOptions options) throws Exception {

		final DocumentCacheService cache = new DocumentCacheService(options.getDocCachePath());
		final TaxonGrab nameGrabber = new TaxonGrab();
		final File outputFile = new File(options.getOutputFile());
		
		if (outputFile.exists()) {
			outputFile.delete();
		}
		
		final Writer writer = new FileWriter(outputFile);

		cache.forEachItemPage(new CachedItemPageHandler() {
			
			private String _language = "";
			
			
			public void onPage(String itemId, String pageId, File pageFile) {

				try {
					String text = FileUtils.readFileToString(pageFile);
					LanguageScore score = WordLists.detectLanguage(text, _language);
					String lang = _language;					
					if (score != null &&  ! StringUtils.equalsIgnoreCase(score.getName(), _language) && score.getScore() > .75) {
						log("Page %s - %s language detected as %s (scored %g) - This conflicts with meta data language of %s", itemId, pageId, score.getName(), score.getScore(), _language);
						lang = score.getName();
						if (score.getScore() == 1.0) {
							System.err.println("Here");
						}
					}
					
					List<String> names = nameGrabber.findNames(text, lang);
					for (String name : names) {
						String line = String.format("%s,%s,\"%s\",\"%s\"\n", itemId, pageId, name, pageFile.getName());
						writer.write(line);
					}
				} catch (IOException ioex) {
					throw new RuntimeException(ioex);
				}

			}

			public void startItem(String itemId) {
				CacheControlBlock ccb = cache.getCacheControl(itemId);
				if (ccb != null) {
					_language = ccb.Language;
				}
				log("Starting item %s (%s)", itemId, _language);
			}

			public void endItem(String itemId) {
				try {
					writer.flush();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				_language = "";				
			}
			
		});
	}

	public void defineOptions(Options options) {
		options.addOption("o", true, "Output file for dumps, reports etc");
	}

}
