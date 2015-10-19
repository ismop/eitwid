package pl.ismop.web.client.dap.timeline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fusesource.restygwt.client.Json;
import pl.ismop.web.client.dap.parameter.Parameter;

public class Timeline {
	private String id;
	
	@Json(name = "parameter_id")
	private String parameterId;
	
	@Json(name = "context_id")
	private String contextId;

	@JsonIgnore
	private Parameter parameter;

	@JsonIgnore
	private String label;

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

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public Parameter getParameter() {
		return parameter;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}