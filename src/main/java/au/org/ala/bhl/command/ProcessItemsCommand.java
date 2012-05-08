package au.org.ala.bhl.command;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;

import java.io.File;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.service.CachedItemPageHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;

@Command(name = "process-items")
public class ProcessItemsCommand extends AbstractCommand {

	public void execute(ItemsService service, IndexerOptions options) throws Exception {

		String scriptFile = options.getScript();

		if (StringUtils.isEmpty(scriptFile)) {
			throw new RuntimeException("No script file specified. Use the the -script option");
		}

		File f = new File(scriptFile);
		if (!f.exists()) {
			throw new RuntimeException(String.format("Script file not found: %s", scriptFile));
		}

		Binding binding = new Binding();
		GroovyShell shell = new GroovyShell(binding);

		shell.evaluate(f);

		final Closure<Void> startItem = extractClosureVar(binding, "startItem");
		final Closure endItem = extractClosureVar(binding, "endItem");
		final Closure onPage = extractClosureVar(binding, "onPage");

		final DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());

		docCache.forEachItemPage(new CachedItemPageHandler() {

			public void startItem(String itemId) {
				if (startItem != null) {
					startItem.call(itemId, docCache);
				}
			}

			public void onPage(String iaId, String pageId, String text) {
				if (onPage != null) {
					onPage.call(iaId, pageId, text);
				}
			}

			public void endItem(String itemId) {
				if (endItem != null) {
					endItem.call(itemId, docCache);
				}
			}

		});

	}

	@SuppressWarnings("unchecked")
	private Closure<Void> extractClosureVar(Binding binding, String name) {
		if (binding.getVariables().containsKey(name)) {
			Object closure = binding.getVariable(name);
			if (closure instanceof Closure) {
				return (Closure<Void>) closure;
			}
		}
		return null;
	}

	public void defineOptions(Options options) {
		options.addOption("script", true, "The groovy script to execute for each item");
	}

}
