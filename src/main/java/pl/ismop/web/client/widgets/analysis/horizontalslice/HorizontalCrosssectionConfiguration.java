package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.dap.section.Section;

public class HorizontalCrosssectionConfiguration {
	private Set<String> parameterNames;

	private String pickedParameterName;

	private Map<Section, List<Device>> sectionDevicesMap;

	private Map<String, Parameter> parameterMap;

	private Map<Section, String> pickedHeights;

	private Map<String, List<Device>> heightDevicesmap;

	private Map<Section, List<String>> profileHeights;

	private Map<String, Section> sections;

	private Experiment experiment;

	private Map<String, Scenario> scenarioMap;

	private String dataSelector;

	private boolean budokopProfiles;

	private Map<String, Section> pickedSections;

	public HorizontalCrosssectionConfiguration() {
		parameterNames = new HashSet<>();
		setPickedSections(new HashMap<>());
		setSectionDevicesMap(new HashMap<>());
		parameterMap = new HashMap<>();
		pickedHeights = new HashMap<>();
		heightDevicesmap = new HashMap<>();
		profileHeights = new HashMap<>();
		sections = new HashMap<>();
		scenarioMap = new HashMap<>();
		setBudokopProfiles(true);
	}

	public Set<String> getParameterNames() {
		return parameterNames;
	}

	public void setParameterNames(Set<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	public String getPickedParameterMeasurementName() {
		return pickedParameterName;
	}

	public void setPickedParameterMeasurementName(String pickedParameterName) {
		this.pickedParameterName = pickedParameterName;
	}

	public Map<String, Parameter> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, Parameter> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public Map<Section, String> getPickedHeights() {
		return pickedHeights;
	}

	public void setPickedHeights(Map<Section, String> pickedHeights) {
		this.pickedHeights = pickedHeights;
	}

	public Map<String, List<Device>> getHeightDevicesmap() {
		return heightDevicesmap;
	}

	public void setHeightDevicesmap(Map<String, List<Device>> heightDevicesmap) {
		this.heightDevicesmap = heightDevicesmap;
	}

	public Map<Section, List<String>> getSectionHeights() {
		return profileHeights;
	}

	public void setProfileHeights(Map<Section, List<String>> profileHeights) {
		this.profileHeights = profileHeights;
	}

	public Map<String, Section> getSections() {
		return sections;
	}

	public void setSections(Map<String, Section> sections) {
		this.sections = sections;
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

	public String getDataSelector() {
		return dataSelector;
	}

	public void setDataSelector(String dataSelector) {
		this.dataSelector = dataSelector;
	}

	public boolean isBudokopProfiles() {
		return budokopProfiles;
	}

	public void setBudokopProfiles(boolean budokopProfiles) {
		this.budokopProfiles = budokopProfiles;
	}

	public Map<String, Section> getPickedSections() {
		return pickedSections;
	}

	public void setPickedSections(Map<String, Section> pickedSections) {
		this.pickedSections = pickedSections;
	}

	public Map<Section, List<Device>> getSectionDevicesMap() {
		return sectionDevicesMap;
	}

	public void setSectionDevicesMap(Map<Section, List<Device>> sectionDevicesMap) {
		this.sectionDevicesMap = sectionDevicesMap;
	}
}
