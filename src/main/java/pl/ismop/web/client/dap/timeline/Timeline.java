package pl.ismop.web.client.dap.timeline;

import org.fusesource.restygwt.client.Json;

public class Timeline {
	private String id;
	
	@Json(name = "parameter_id")
	private String parameterId;
	
	@Json(name = "context_id")
	private String contextId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}
}