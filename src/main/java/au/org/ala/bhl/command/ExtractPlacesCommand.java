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

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.PlaceGrab;
import au.org.ala.bhl.service.CachedItemPageHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;

/**
 * Experimental command that attempts to locate localities (place names, lat longs etc) from the ocr text held in the document cache
 * 
 * @author baird
 * 
 */
@Command(name = "extract-places")
public class ExtractPlacesCommand extends AbstractCommand {

	public void execute(ItemsService service, IndexerOptions options) throws Exception {
		final DocumentCacheService cache = new DocumentCacheService(options.getDocCachePath());
		final File outputFile = new File(options.getOutputFile());

		if (outputFile.exists()) {
			outputFile.delete();
		}

		final Writer writer = new FileWriter(outputFile);

		cache.forEachItemPage(new CachedItemPageHandler() {

			public void onPage(String internetArchiveId, String pageId, String text) {

				try {
					List<String> places = PlaceGrab.findPlaces(text);
					for (String place : places) {
						String line = String.format("%s,%s,\"%s\"\n", internetArchiveId, pageId, place);
						writer.write(line);
						System.err.println(place);
					}
				} catch (IOException ioex) {
					throw new RuntimeException(ioex);
				}

			}

			public void startItem(String internetArchiveId) {
				log("Starting item %s", internetArchiveId);
			}

			public void endItem(String itemId) {
				try {
					writer.flush();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		});

	}

	/**
	 * 
	 */
	public void defineOptions(Options options) {
	}

}
