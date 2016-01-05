package pl.ismop.web.client.dap.monitoring;

import java.util.List;

import pl.ismop.web.client.dap.parameter.Parameter;

public class MonitoringResponse {
	private List<Parameter> parameters;

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
}