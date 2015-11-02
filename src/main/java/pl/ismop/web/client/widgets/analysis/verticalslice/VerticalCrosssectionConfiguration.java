package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;

public class VerticalCrosssectionConfiguration {
	private Profile pickedProfile;
	
	private Map<Profile, List<Device>> profileDevicesMap;
	
	private Map<String, Parameter> parameterMap;
	
	private Set<String> parameterNames;
	
	private String pickedParameterName;
	
	public VerticalCrosssectionConfiguration() {
		profileDevicesMap = new HashMap<>();
		parameterMap = new HashMap<>();
		parameterNames = new HashSet<>();
	}

	public Profile getPickedProfile() {
		return pickedProfile;
	}

	public void setPickedProfile(Profile pickedProfile) {
		this.pickedProfile = pickedProfile;
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
}