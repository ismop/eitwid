package pl.ismop.web.client.dap.deviceaggregation;

import java.util.List;

import org.fusesource.restygwt.client.Json;

public class DeviceAggregation {
	private String id;
	
	@Json(name = "parent_id")
	private String parentId;
	
	@Json(name = "children_ids")
	private List<String> childernIds;
	
	private String type;
	
	private PointShape placement;
	
	@Json(name = "profile_id")
	private String profileId;
	
	@Json(name = "section_id")
	private String sectionId;
	
	@Json(name = "levee_id")
	private String leveeId;
	
	@Json(name = "device_ids")
	private List<String> deviceIds;

	public String getId() {
		return id;
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

	public List<String> getChildernIds() {
		return childernIds;
	}

	public void setChildernIds(List<String> childernIds) {
		this.childernIds = childernIds;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public PointShape getPlacement() {
		return placement;
	}

	public void setPlacement(PointShape placement) {
		this.placement = placement;
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
}