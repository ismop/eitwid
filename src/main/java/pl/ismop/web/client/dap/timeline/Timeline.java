package pl.ismop.web.client.dap.timeline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fusesource.restygwt.client.Json;
import pl.ismop.web.client.dap.parameter.Parameter;
import java.util.Date;

public class Timeline {
	private String id;
	
	@Json(name = "parameter_id")
	private String parameterId;
	
	@Json(name = "context_id")
	private String contextId;

	@Json(name = "scenario_id")
	private String scenarioId;

	@Json(name = "experiment_id")
	private String experimentId;

	@Json(name = "earliest_measurement_timestamp")
	private Date earliestMeasurementTimestamp;

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

	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
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

	public Date getEarliestMeasurementTimestamp() {
		return earliestMeasurementTimestamp;
	}

	public void setEarliestMeasurementTimestamp(Date earliestMeasurementTimestamp) {
		this.earliestMeasurementTimestamp = earliestMeasurementTimestamp;
	}
}