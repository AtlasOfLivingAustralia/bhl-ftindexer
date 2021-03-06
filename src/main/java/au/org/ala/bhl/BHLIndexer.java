package au.org.ala.bhl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ala.bhl.command.CacheStatsCommand;
import au.org.ala.bhl.command.CommandLineCommand;
import au.org.ala.bhl.command.CompressPagesCommand;
import au.org.ala.bhl.command.ExtractNamesCommand;
import au.org.ala.bhl.command.ExtractPlacesCommand;
import au.org.ala.bhl.command.ImportItemsCommand;
import au.org.ala.bhl.command.IndexItemsCommand;
import au.org.ala.bhl.command.MaintainCacheCommand;
import au.org.ala.bhl.command.ProcessItemsCommand;
import au.org.ala.bhl.command.ResetStatusCommand;
import au.org.ala.bhl.command.RetrieveItemsCommand;
import au.org.ala.bhl.command.StatisticsCommand;
import au.org.ala.bhl.command.UpdateCacheControlCommand;
import au.org.ala.bhl.command.UpdateItemStatus;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.service.LogService;

/**
 * Entry point to the BHL Full Text Index utility program. The program accepts a command and a number of optional arguments.
 * Each command is implement as a separate 'command' class, and is registered in the command map (see the static initialiser).
 * 
 * @author baird
 *
 */
public class BHLIndexer {

	private static Map<String, CommandLineCommand> _commandMap;

	static {
		_commandMap = new HashMap<String, CommandLineCommand>();
		registerCommand(ImportItemsCommand.class);
		registerCommand(IndexItemsCommand.class);
		registerCommand(MaintainCacheCommand.class);
		registerCommand(StatisticsCommand.class);
		// registerCommand(PaginateDocCacheCommand.class);
		registerCommand(RetrieveItemsCommand.class);
		registerCommand(ExtractNamesCommand.class);
		registerCommand(ExtractPlacesCommand.class);
		registerCommand(UpdateCacheControlCommand.class);
		registerCommand(CacheStatsCommand.class);
		registerCommand(ResetStatusCommand.class);
		registerCommand(ProcessItemsCommand.class);
		registerCommand(CompressPagesCommand.class);
		registerCommand(UpdateItemStatus.class);
	}

	private static void registerCommand(Class<? extends CommandLineCommand> clazz) {
		Command cmd = clazz.getAnnotation(Command.class);
		if (cmd != null) {
			try {
				CommandLineCommand impl = clazz.newInstance();
				_commandMap.put(cmd.name(), impl);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			throw new RuntimeException("Failed to register command class (No annotation!): " + clazz.getName());
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		System.setProperty("java.util.logging.config.file", "logging.properties");
		
		Logger logger = LoggerFactory.getLogger("");
	    logger.info("Hello World");
	    
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				log("Shutting down.");
			}
		});

		CommandLineParser parser = new GnuParser();

		Options options = defineOptions();

		CommandLine line = parser.parse(options, args);

		if (line.getArgs().length != 1) {
			usage(options);
			System.exit(1);
		}

		String cmdStr = line.getArgs()[0];
		if (_commandMap.containsKey(cmdStr)) {
			log("Processing command: %s", cmdStr);
			try {
				ItemsService service = new ItemsService();
				IndexerOptions indexerOptions = new IndexerOptions(line);
				dumpOptions(indexerOptions);
				Timer t = new Timer(cmdStr);				
				_commandMap.get(cmdStr).execute(service, indexerOptions);
				t.stop(false);
				log("Command complete: %s (%d ms)", cmdStr, t.getElapsedMillis());
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			
		} else {
			System.out.println(String.format("Unrecognized command: %s", cmdStr));
			usage(options);
			System.exit(1);
		}

	}

	private static void dumpOptions(IndexerOptions options) {
		log("Local doc cache path: %s", options.getDocCachePath());
		log("Thread count: %s", options.getThreadCount());
		log("SOLR URL: %s", options.getSolrServerURL());
		log("Filter: %s", options.getItemFilter());
	}

	private static Options defineOptions() {

		Options options = new Options();

		options.addOption("doccache", true, "The path to the root of the local document cache (Common)");

		for (CommandLineCommand cmd : _commandMap.values()) {
			cmd.defineOptions(options);
		}

		return options;
	}

	private static void usage(Options options) {
		HelpFormatter f = new HelpFormatter();
		f.setWidth(200);
		f.printHelp(String.format("java au.org.ala.bhl.BHLIndexer %s <options>", StringUtils.join(_commandMap.keySet(), "|")), options);
	}

	private static void log(String format, Object... args) {
		LogService.log(BHLIndexer.class, format, args);
	}

}
