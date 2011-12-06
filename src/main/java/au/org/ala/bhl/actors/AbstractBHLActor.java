package au.org.ala.bhl.actors;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import akka.actor.UntypedActor;
import au.org.ala.bhl.service.ItemSourceService;
import au.org.ala.bhl.service.LogService;

public abstract class AbstractBHLActor extends UntypedActor {
    
    private ItemSourceService _service;
    
    protected void log(String format, Object ... args) {
        LogService.log(this.getClass(), format, args);        
    }
    
    protected ItemSourceService getItemsService() {
        if (_service == null) {
            _service = new ItemSourceService();
        }
        return _service;
    }
    
    protected JsonNode webServiceCallJson(String uri) throws IOException {               
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri);
        httpget.setHeader("Accept", "application/json");
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream instream = entity.getContent();
            @SuppressWarnings("unchecked")
            List<String> lines = IOUtils.readLines(instream, "utf-8");            
            JsonNode root = new ObjectMapper().readValue(StringUtils.join(lines, "\n"), JsonNode.class);
            return root; 
        }        
        return null;
    }
    
    protected String webGetText(String uri) throws IOException {               
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri);
        httpget.setHeader("Accept", "application/text");
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream instream = entity.getContent();
            @SuppressWarnings("unchecked")
            List<String> lines = IOUtils.readLines(instream, "utf-8");            
            return StringUtils.join(lines, "\n");            
        }        
        return null;
    }
    


}
