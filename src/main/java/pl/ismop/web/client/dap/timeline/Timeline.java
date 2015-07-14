package pl.ismop.web.client.dap.timeline;

import org.fusesource.restygwt.client.Json;

public class Timeline {
	private String id;
	
	@Json(name = "paramter_id")
	private String paramterId;
	
	@Json(name = "context_id")
	private String contextId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParamterId() {
		return paramterId;
	}

	public void setParamterId(String paramterId) {
		this.paramterId = paramterId;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}
}