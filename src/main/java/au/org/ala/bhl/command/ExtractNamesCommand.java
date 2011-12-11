package au.org.ala.bhl.command;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.TaxonGrab;
import au.org.ala.bhl.service.CachedItemPageHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;

@Command(name = "extract-names")
public class ExtractNamesCommand extends AbstractCommand {
	
	

	public void execute(ItemsService service, IndexerOptions options) throws Exception {

		DocumentCacheService cache = new DocumentCacheService(options.getDocCachePath());
		final TaxonGrab nameGrabber = new TaxonGrab();
		final File outputFile = new File(options.getOutputFile());

		cache.forEachItemPage(new CachedItemPageHandler() {
			public void onPage(String itemId, String pageId, File pageFile) {

				try {
					String text = FileUtils.readFileToString(pageFile);
					List<String> names = nameGrabber.findNames(text);
					for (String name : names) {
						String line = String.format("%s,%s,\"%s\",\"%s\"", itemId, pageId, name, pageFile.getName());
						FileUtils.writeStringToFile(outputFile, line);
						System.err.println(line);
					}
				} catch (IOException ioex) {
					throw new RuntimeException(ioex);
				}

			}
		});
	}

	public void defineOptions(Options options) {
		options.addOption("o", true, "Output file for dumps, reports etc");
	}

}
