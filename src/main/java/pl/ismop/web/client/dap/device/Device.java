package pl.ismop.web.client.dap.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.Json;

import pl.ismop.web.client.dap.deviceaggregation.PointShape;
import pl.ismop.web.client.geojson.Geometry;
import pl.ismop.web.client.geojson.MapFeature;
import pl.ismop.web.client.geojson.PointGeometry;

public class Device extends MapFeature {
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

	@Json(name = "metadata")
	private Map<String, String> metadata;

	private String vendor;

	@Override
    public String getId() {
		return id;
	}

	@Override
	public String getFeatureType() {
		return "device";
	}

	@Override
	public Geometry getFeatureGeometry() {
		if (getPlacement() != null) {
			PointGeometry pointGeometry = new PointGeometry();
			pointGeometry.setCoordinates(getPlacement().getCoordinates());
			return pointGeometry;
		} else {
			return null;
		}
	}

	@Override
	public Map<String, String> getAdditionalFeatureProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("colour_type", getVendor());

		return properties;
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

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public Float getLeveeDistanceMarker() {
		return getFloat(getMetadata().get("levee_distance_marker"), 0f);
	}

	public Float getCableDistanceMarker() {
		return getFloat(getMetadata().get("cable_distance_marker"), 0f);
	}

	private Float getFloat(String stringValue, Float defaultValue) {
		if (stringValue != null) {
			return Float.parseFloat(stringValue);
		} else {
			return defaultValue;
		}
	}

    @Override
    public String getName() {
        return customId;
    }
}