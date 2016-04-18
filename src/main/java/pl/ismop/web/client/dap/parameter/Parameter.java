package pl.ismop.web.client.dap.parameter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fusesource.restygwt.client.Json;
import pl.ismop.web.client.dap.device.Device;

public class Parameter {
	private String id;
	
	@Json(name = "custom_id")
	private String customId;
	
	@Json(name = "parameter_name")
	private String parameterName;
	
	@Json(name = "device_id")
	private String deviceId;
	
	@Json(name = "measurement_type_name")
	private String measurementTypeName;
	
	@Json(name = "measurement_type_unit")
	private String measurementTypeUnit;
	
	@Json(name = "timeline_ids")
	private List<String> timelineIds;

	@JsonIgnore
	private Device device;
	
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

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
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

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		return true;
	}
}