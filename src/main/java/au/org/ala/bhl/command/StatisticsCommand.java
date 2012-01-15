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
import java.util.Map;

import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.Command;
import au.org.ala.bhl.IndexerOptions;
import au.org.ala.bhl.ItemTOHandler;
import au.org.ala.bhl.service.DocumentCacheService;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.to.ItemTO;

@Command(name = "stats")
public class StatisticsCommand extends AbstractCommand {

    public void execute(final ItemsService service, final IndexerOptions options) throws Exception {
        StatsCounter counter = new StatsCounter(options);
        service.forAllItems(counter, createItemFilter(options));
        counter.dump();
    }

    public void defineOptions(Options options) {
    }

    class StatsCounter implements ItemTOHandler {

        private int _total = 0;
        private int _cached = 0;
        protected IndexerOptions _options;
        private Map<String, Integer> _statusMap;
        private DocumentCacheService _docCache;

        public StatsCounter(IndexerOptions options) {
            _options = options;
            _statusMap = new HashMap<String, Integer>();
            _docCache = new DocumentCacheService(options.getDocCachePath());
        }

        public void onItem(ItemTO item) {
            _total++;
            File f = new File(_docCache.getItemDirectoryPath(item));
            if (f.exists()) {
                _cached++;
            }

            if (StringUtils.isEmpty(item.getStatus())) {
                incrementStatus("NoStatus");
            } else {
                incrementStatus(item.getStatus());
            }

        }

        private void incrementStatus(String status) {
            if (!_statusMap.containsKey(status)) {
                _statusMap.put(status, 0);
            }

            _statusMap.put(status, _statusMap.get(status) + 1);
        }

        public void dump() {
            System.out.printf("Total items\t: %d\n", _total);
            System.out.printf("Cached items\t: %d\n", _cached);
            for (String key : _statusMap.keySet()) {
                System.out.printf("Status %s\t: %d\n", key, _statusMap.get(key));
            }
        }

    }

}
