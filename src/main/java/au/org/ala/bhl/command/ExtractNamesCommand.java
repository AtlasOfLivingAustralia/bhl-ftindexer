/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
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

/**
 * Experimental command that attempts to identify taxonomic names in the OCR'd text held in the document cache
 * 
 * @author baird
 *
 */
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
			
			
			public void onPage(String internetArchiveId, String pageId, File pageFile) {

				try {
					String text = FileUtils.readFileToString(pageFile);
					LanguageScore score = WordLists.detectLanguage(text, _language);
					String lang = _language;					
					if (score != null &&  ! StringUtils.equalsIgnoreCase(score.getName(), _language) && score.getScore() > .75) {
						log("Page %s - %s language detected as %s (scored %g) - This conflicts with meta data language of %s", internetArchiveId, pageId, score.getName(), score.getScore(), _language);
						lang = score.getName();
						if (score.getScore() == 1.0) {
							System.err.println("Here");
						}
					}
					
					List<String> names = nameGrabber.findNames(text, lang);
					for (String name : names) {
						String line = String.format("%s,%s,\"%s\",\"%s\"\n", internetArchiveId, pageId, name, pageFile.getName());
						writer.write(line);
					}
				} catch (IOException ioex) {
					throw new RuntimeException(ioex);
				}

			}

			public void startItem(String internetArchiveId) {
				CacheControlBlock ccb = cache.getCacheControl(internetArchiveId);
				if (ccb != null) {
					_language = ccb.Language;
				}
				log("Starting item %s (%s)", internetArchiveId, _language);
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
