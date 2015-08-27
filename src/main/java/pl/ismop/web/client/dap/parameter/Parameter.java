package pl.ismop.web.client.dap.parameter;

import java.util.List;

import org.fusesource.restygwt.client.Json;

public class Parameter {
	private String id;
	
	@Json(name = "custom_id")
	private String customId;
	
	@Json(name = "parameter_name")
	private String paramterName;
	
	@Json(name = "device_id")
	private String deviceId;
	
	@Json(name = "measurement_type_name")
	private String measurementTypeName;
	
	@Json(name = "measurement_type_unit")
	private String measurementTypeUnit;
	
	@Json(name = "timeline_ids")
	private List<String> timelineIds;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCustomId() {
		return customId;
	}

	public void setCustomId(String customId) {
		this.customId = customId;
	}

	public String getParamterName() {
		return paramterName;
	}

	public void setParamterName(String paramterName) {
		this.paramterName = paramterName;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getMeasurementTypeName() {
		return measurementTypeName;
	}

	public void setMeasurementTypeName(String measurementTypeName) {
		this.measurementTypeName = measurementTypeName;
	}

	public String getMeasurementTypeUnit() {
		return measurementTypeUnit;
	}

	public void setMeasurementTypeUnit(String measurementTypeUnit) {
		this.measurementTypeUnit = measurementTypeUnit;
	}

	public List<String> getTimelineIds() {
		return timelineIds;
	}

	public void setTimelineIds(List<String> timelineIds) {
		this.timelineIds = timelineIds;
	}
}