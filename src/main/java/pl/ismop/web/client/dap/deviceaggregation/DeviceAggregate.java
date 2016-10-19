package pl.ismop.web.client.dap.deviceaggregation;

import org.fusesource.restygwt.client.Json;
import pl.ismop.web.client.geojson.Geometry;
import pl.ismop.web.client.geojson.MapFeature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceAggregate extends MapFeature {
	private String id;
	
	@Json(name = "parent_id")
	private String parentId;
	
	@Json(name = "children_ids")
	private List<String> childernIds;
	
	private String type;
	
	@Json(name = "profile_id")
	private String profileId;
	
	@Json(name = "section_id")
	private String sectionId;
	
	@Json(name = "levee_id")
	private String leveeId;
	
	@Json(name = "device_ids")
	private List<String> deviceIds;

	@Json(name = "custom_id")
	private String customId;
	
	private Geometry shape;

	private List<String> vendors;

	public String getId() {
		return id;
	}

	@Override
	public String getFeatureType() {
		return "deviceAggregate";
	}

	@Override
	public Geometry getFeatureGeometry() {
		return getShape();
	}

	@Override
	public Map<String, String> getAdditionalFeatureProperties() {
		Map<String, String> properties = new HashMap<>();
		if (getVendors() != null && getVendors().size() > 0) {
			properties.put("colour_type", getVendors().get(0));
		}

		return properties;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public List<String> getChildrenIds() {
		return childernIds;
	}

	public void setChildrenIds(List<String> childrenIds) {
		this.childernIds = childrenIds;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public List<String> getDeviceIds() {
		return deviceIds;
	}

	public void setDeviceIds(List<String> deviceIds) {
		this.deviceIds = deviceIds;
	}

	public Geometry getShape() {
		return shape;
	}

	public void setShape(Geometry shape) {
		this.shape = shape;
	}

	public String getCustomId() {
		return customId;
	}

	public void setCustomId(String customId) {
		this.customId = customId;
	}

	public List<String> getVendors() {
		return vendors;
	}

	public void setVendors(List<String> vendors) {
		this.vendors = vendors;
	}
}