package au.org.ala.bhl.command;

import org.apache.commons.cli.Options;

import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.service.ItemSourceService;

public interface CommandLineCommand {
    
    void execute(final ItemSourceService service, final IndexerOptions options) throws Exception;
    void defineOptions(final Options options);
    
}
