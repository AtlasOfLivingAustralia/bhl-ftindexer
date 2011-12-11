package au.org.ala.bhl.actors;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;

import akka.actor.UntypedActor;
import au.org.ala.bhl.service.ItemsService;
import au.org.ala.bhl.service.LogService;
import au.org.ala.bhl.service.WebServiceHelper;

public abstract class AbstractBHLActor extends UntypedActor {

	private ItemsService _service;

	protected void log(String format, Object... args) {
		LogService.log(this.getClass(), format, args);
	}

	protected ItemsService getItemsService() {
		if (_service == null) {
			_service = new ItemsService();
		}
		return _service;
	}

	protected JsonNode webServiceCallJson(String uri) throws IOException {
		return WebServiceHelper.getJSON(uri);
	}

	protected String webGetText(String uri) throws IOException {
		return WebServiceHelper.getText(uri);
	}

}
