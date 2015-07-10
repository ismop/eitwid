package pl.ismop.web.client.dap.device;

import java.util.List;

import org.fusesource.restygwt.client.Json;

import pl.ismop.web.client.dap.deviceaggregation.PointShape;

public class Device {
	private String id;
	
	@Json(name = "custom_id")
	private String customId;
	
	private PointShape placement;
	
	@Json(name = "device_type")
	private String deviceType;
	
	@Json(name = "device_aggregation_id")
	private String deviceAggregationId;
	
	@Json(name = "profile_id")
	private String profileId;
	
	@Json(name = "section_id")
	private String sectionId;
	
	@Json(name = "levee_id")
	private String leveeId;
	
	@Json(name = "neosentio_sensor_id")
	private String neosentioSensorId;
	
	@Json(name = "budokop_sensor_id")
	private String budokopSensorId;
	
	@Json(name = "pump_id")
	private String pumpId;
	
	@Json(name = "parameter_ids")
	private List<String> parameterIds;

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

	public PointShape getPlacement() {
		return placement;
	}

	public void setPlacement(PointShape placement) {
		this.placement = placement;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceAggregationId() {
		return deviceAggregationId;
	}

	public void setDeviceAggregationId(String deviceAggregationId) {
		this.deviceAggregationId = deviceAggregationId;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public String getLeveeId() {
		return leveeId;
	}

	public void setLeveeId(String leveeId) {
		this.leveeId = leveeId;
	}

	public String getNeosentioSensorId() {
		return neosentioSensorId;
	}

	public void setNeosentioSensorId(String neosentioSensorId) {
		this.neosentioSensorId = neosentioSensorId;
	}

	public String getBudokopSensorId() {
		return budokopSensorId;
	}

	public void setBudokopSensorId(String budokopSensorId) {
		this.budokopSensorId = budokopSensorId;
	}

	public String getPumpId() {
		return pumpId;
	}

	public void setPumpId(String pumpId) {
		this.pumpId = pumpId;
	}

	public List<String> getParameterIds() {
		return parameterIds;
	}

	public void setParameterIds(List<String> parameterIds) {
		this.parameterIds = parameterIds;
	}
}