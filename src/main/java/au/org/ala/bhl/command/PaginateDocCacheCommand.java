package au.org.ala.bhl.command;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.DocumentPaginator;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.PageHandler;
import au.org.ala.bhl.service.ItemsService;

@Command(name = "paginate")
public class PaginateDocCacheCommand extends AbstractCommand {

	public void execute(ItemsService service, final IndexerOptions options) throws Exception {

		File dir = new File(options.getDocCachePath());

		if (dir.exists() && dir.isDirectory()) {

			dir.listFiles(new FileFilter() {

				public boolean accept(File candidate) {
					if (candidate.length() > 0 && candidate.getName().toLowerCase().endsWith(".xml")) {
						paginateFile(candidate, options);
					}
					return false;
				}
			});

		}

	}

	private void paginateFile(File file, IndexerOptions options) {		
		log("Paginating file: %s", file.getAbsoluteFile());
		DocumentPaginator paginator = new DocumentPaginator();

		String dirname =  String.format("%s\\%s", options.getDocCachePath(), file.getName().substring(0, file.getName().length() - 4));

		final File documentDir = new File(dirname);
		
		if (!documentDir.exists()) {
			log("Creating directory: %s", documentDir.getAbsoluteFile());
			documentDir.mkdir();

		}

		if (documentDir.exists() && documentDir.isDirectory()) {

			paginator.paginate(file.getAbsolutePath(), new PageHandler() {

				public void handlePage(String pageId, String pageText) {
					String pagePath = String.format("%s\\%s.txt", documentDir.getAbsolutePath(), pageId);
					try {
						File f = new File(pagePath);
						if (!f.exists()) {
							FileUtils.writeStringToFile(f, pageText);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}

			});
		} else {

		}
	}

	public void defineOptions(Options options) {
	}

}
