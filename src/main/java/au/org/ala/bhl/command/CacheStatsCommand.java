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
import java.util.HashMap;

import org.apache.commons.cli.Options;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.service.CachedItemHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;

/**
 * cache-stats dumps a brief report of the state of the document-cache and index as held in the database
 * 
 * @author baird
 *
 */
@Command(name = "cache-stats")
public class CacheStatsCommand extends AbstractCommand {

	public void execute(ItemsService service, IndexerOptions options) throws Exception {
		DocumentCacheService docCache = new DocumentCacheService(options.getDocCachePath());

		final CacheStats stats = new CacheStats();
		final ObjectMapper objectMapper = new ObjectMapper();

		docCache.forEachItem(new CachedItemHandler() {

			public void onProgress(int countComplete, int percentComplete) {
				log("%d items processed (%d%%)", countComplete, percentComplete);
			}

			public void onItem(File itemDir) {
				stats.Total++;
				try {
					String mdPath = String.format("%s\\.metadata", itemDir.getAbsolutePath());
					File mdfile = new File(mdPath);
					if (mdfile.exists()) {
						JsonNode md = objectMapper.readTree(mdfile);
						String language = md.get("Language").getTextValue();
						if (!stats.Languages.containsKey(language)) {
							stats.Languages.put(language, new IntCounter());
						}						
						stats.Languages.get(language).increment();
					} else {
						stats.MissingMetaData++;
					}
				} catch (Exception ex) {
					stats.Errors++;
					ex.printStackTrace();
				}
			}
			
			
		});
		
		log("Total items processed: %d", stats.Total);
		log("Items without metadata: %d", stats.MissingMetaData);
		log("Errors: %d", stats.Errors);
		for (String key : stats.Languages.keySet()) {
			IntCounter c = stats.Languages.get(key);
			log("Language '%s': %d", key, c.getCount());
		}
	}

	public void defineOptions(Options options) {
	}

	class CacheStats {
		int MissingMetaData;
		int Total;
		int Errors;
		HashMap<String, IntCounter> Languages = new HashMap<String, IntCounter>();
	}
	
	class IntCounter {
		private int _count;
		
		public void increment() {
			_count++;
		}
		
		public void decrement() {
			_count--;
		}
		
		public void setCount(int count) {
			this._count = count; 
		}
		
		public int getCount() {
			return _count;
		}
	}

}
