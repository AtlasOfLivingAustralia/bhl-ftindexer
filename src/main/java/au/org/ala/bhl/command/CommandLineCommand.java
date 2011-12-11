package au.org.ala.bhl.command;

import org.apache.commons.cli.Options;

import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.service.ItemsService;

public interface CommandLineCommand {
    
    void execute(final ItemsService service, final IndexerOptions options) throws Exception;
    void defineOptions(final Options options);
    
}
