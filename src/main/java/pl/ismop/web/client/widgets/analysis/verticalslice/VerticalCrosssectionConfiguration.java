package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.scenario.Scenario;

public class VerticalCrosssectionConfiguration {
	private Profile pickedProfile;
	
	private Map<Profile, List<Device>> profileDevicesMap;
	
	private Map<String, Parameter> parameterMap;
	
	private Set<String> parameterNames;
	
	private String pickedParameterName;
	
	private String dataSelector;
	
	private Map<String, Scenario> scenarioMap;
	
	private Experiment experiment;
	
	public VerticalCrosssectionConfiguration() {
		profileDevicesMap = new HashMap<>();
		parameterMap = new HashMap<>();
		parameterNames = new HashSet<>();
		scenarioMap = new HashMap<>();
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

	public String getDataSelector() {
		return dataSelector;
	}

	public void setDataSelector(String dataSelector) {
		this.dataSelector = dataSelector;
	}

	public Map<String, Scenario> getScenarioMap() {
		return scenarioMap;
	}

	public void setScenarioMap(Map<String, Scenario> scenarioMap) {
		this.scenarioMap = scenarioMap;
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}
}