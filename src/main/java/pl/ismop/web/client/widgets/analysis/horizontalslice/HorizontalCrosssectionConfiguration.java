package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;

public class HorizontalCrosssectionConfiguration {
	private Set<String> parameterNames;
	
	private String pickedParameterName;
	
	private Map<String, Profile> pickedProfiles;
	
	private Map<Profile, List<Device>> profileDevicesMap;
	
	private Map<String, Parameter> parameterMap;
	
	private Map<Profile, String> pickedHeights;
	
	private Map<String, List<Device>> heightDevicesmap;
	
	private Map<Profile, List<String>> profileHeights;
	
	private Map<String, Section> sections;
	
	public HorizontalCrosssectionConfiguration() {
		parameterNames = new HashSet<>();
		pickedProfiles = new HashMap<>();
		profileDevicesMap = new HashMap<>();
		parameterMap = new HashMap<>();
		pickedHeights = new HashMap<>();
		heightDevicesmap = new HashMap<>();
		profileHeights = new HashMap<>();
		sections = new HashMap<>();
	}

	public Set<String> getParameterNames() {
		return parameterNames;
	}

	public void setParameterNames(Set<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	public String getPickedParameterName() {
		return pickedParameterName;
	}

	public void setPickedParameterName(String pickedParameterName) {
		this.pickedParameterName = pickedParameterName;
	}

	public Map<String, Profile> getPickedProfiles() {
		return pickedProfiles;
	}

	public void setPickedProfiles(Map<String, Profile> pickedProfiles) {
		this.pickedProfiles = pickedProfiles;
	}

	public Map<Profile, List<Device>> getProfileDevicesMap() {
		return profileDevicesMap;
	}

	public void setProfileDevicesMap(Map<Profile, List<Device>> profileDevicesMap) {
		this.profileDevicesMap = profileDevicesMap;
	}

	public Map<String, Parameter> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Parameter> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public Map<Profile, String> getPickedHeights() {
		return pickedHeights;
	}

	public void setPickedHeights(Map<Profile, String> pickedHeights) {
		this.pickedHeights = pickedHeights;
	}

	public Map<String, List<Device>> getHeightDevicesmap() {
		return heightDevicesmap;
	}

	public void setHeightDevicesmap(Map<String, List<Device>> heightDevicesmap) {
		this.heightDevicesmap = heightDevicesmap;
	}

	public Map<Profile, List<String>> getProfileHeights() {
		return profileHeights;
	}

	public void setProfileHeights(Map<Profile, List<String>> profileHeights) {
		this.profileHeights = profileHeights;
	}

	public Map<String, Section> getSections() {
		return sections;
	}

	public void setSections(Map<String, Section> sections) {
		this.sections = sections;
	}
}