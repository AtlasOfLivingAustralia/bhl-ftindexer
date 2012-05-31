package au.org.ala.bhl.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemDescriptor;
import au.org.ala.bhl.ItemStatus;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.service.LogService;

@Command(name = "update-item-status")
public class UpdateItemStatus extends AbstractCommand {

	public void execute(ItemsService service, IndexerOptions options) throws Exception {
		
        if (StringUtils.isEmpty(options.getSourceFilename())) {
            throw new RuntimeException("No value for source file argument!");            
        }
        
        String sourceFile = options.getSourceFilename();
        File f = new File(sourceFile);
        if (f.exists()) {
            FileInputStream fis = new FileInputStream(f);
            Reader filereader = new InputStreamReader(fis, "ISO-8859-1");
            CSVReader reader = new CSVReader(filereader, ',', '"', 1);
            String[] nextLine;
            try {
                while ((nextLine = reader.readNext()) != null) {
                	String itemId = nextLine[0];
                	String status = nextLine[1];
                	log("Setting %s to status %s", itemId, status);
                	service.setItemStatus(itemId, status);
                }
            } finally {
                reader.close();
                filereader.close();
                fis.close();
            }

        } else {
            throw new RuntimeException("File not found! " + sourceFile);
        }
        

	}
	
	

	public void defineOptions(Options options) {
		options.addOption("sourcefile", true, "Input file for seeding items store (import-items, update-item-status)");
	}

}
