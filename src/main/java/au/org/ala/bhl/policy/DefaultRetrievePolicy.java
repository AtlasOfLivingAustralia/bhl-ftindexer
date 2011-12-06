package au.org.ala.bhl.policy;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import au.org.ala.bhl.to.ItemTO;

public class DefaultRetrievePolicy implements ItemRetrievePolicy {

    public boolean shouldRetrieveItem(ItemTO item) {

        if (!StringUtils.isEmpty(item.getLocalCacheFile())) {
            File cachedFile = new File(item.getLocalCacheFile());
            if (!cachedFile.exists()) {
                return true;
            }
        }

        if (StringUtils.isEmpty(item.getStatus())) {
            return true;
        }

        return false;
    }

}
