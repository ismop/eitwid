package pl.ismop.web.client.dap.context;

import org.fusesource.restygwt.client.Json;

public class Context {
	private String id;
	
	private String name;
	
	@Json(name = "context_type")
	private String contextType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}
}