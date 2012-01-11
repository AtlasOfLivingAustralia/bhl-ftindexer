package au.org.ala.bhl.service;

public abstract class AbstractService {
	
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

}
