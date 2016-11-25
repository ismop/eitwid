package pl.ismop.web.client.widgets.analysis.horizontalslice;

import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.dap.section.Section;

public class HorizontalCrosssectionConfiguration {

	private Experiment experiment;

	private String profileVendor;

	private Seq<String> pickedSectionIds;

	private Map<String, Section> sectionsById;

	private Map<String, Seq<Device>> devicesBySectionId;

	private Map<String, Seq<String>> heightsBySectionId;

	private Map<String, String> pickedHeightsBySectionId;

	private Map<String, Map<String, Seq<Device>>> devicesBySectionIdAndHeight;

	private String pickedParameterName;

	private Map<String, Parameter> parametersById;

	private Map<String, Scenario> scenariosById;

	private String pickedScenarioId;

	public HorizontalCrosssectionConfiguration() {
		pickedSectionIds = List.empty();
		sectionsById = HashMap.empty();
		devicesBySectionId = HashMap.empty();
		heightsBySectionId = HashMap.empty();
		pickedHeightsBySectionId = HashMap.empty();
		devicesBySectionIdAndHeight = HashMap.empty();
		parametersById = HashMap.empty();
		scenariosById = HashMap.empty();
	}

	public Map<String, Section> getSections() {
		return sectionsById;
	}

	public void setSections(Map<String, Section> sections) {
		this.sectionsById = sections;
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public String getProfileVendor() {
		return profileVendor;
	}

	public void setProfileVendor(String profileVendor) {
		this.profileVendor = profileVendor;
	}

	public Seq<String> getPickedSectionIds() {
		return pickedSectionIds;
	}

	public void setPickedSectionIds(Seq<String> pickedSectionIds) {
		this.pickedSectionIds = pickedSectionIds;
	}

	public Map<String, Seq<Device>> getDevicesBySectionId() {
		return devicesBySectionId;
	}

	public void setDevicesBySectionId(Map<String, Seq<Device>> map) {
		this.devicesBySectionId = map;
	}

	public Map<String, String> getPickedHeightsBySectionId() {
		return pickedHeightsBySectionId;
	}

	public void setPickedHeightsBySectionId(Map<String, String> pickedHeightsBySectionId) {
		this.pickedHeightsBySectionId = pickedHeightsBySectionId;
	}

	public Map<String, Seq<String>> getHeightsBySectionId() {
		return heightsBySectionId;
	}

	public void setHeightsBySectionId(Map<String, Seq<String>> heightsBySectionId) {
		this.heightsBySectionId = heightsBySectionId;
	}

	public Map<String, Map<String, Seq<Device>>> getDevicesBySectionIdAndHeight() {
		return devicesBySectionIdAndHeight;
	}

	public void setDevicesBySectionIdAndHeight(Map<String, Map<String, Seq<Device>>> devicesBySectionIdAndHeight) {
		this.devicesBySectionIdAndHeight = devicesBySectionIdAndHeight;
	}

	public String getPickedParameterName() {
		return pickedParameterName;
	}

	public void setPickedParameterName(String pickedParameterName) {
		this.pickedParameterName = pickedParameterName;
	}

	public Map<String, Parameter> getParametersById() {
		return parametersById;
	}

	public void setParametersById(Map<String, Parameter> parametersById) {
		this.parametersById = parametersById;
	}

	public Map<String, Scenario> getScenariosById() {
		return scenariosById;
	}

	public void setScenariosById(Map<String, Scenario> scenariosById) {
		this.scenariosById = scenariosById;
	}

	public String getPickedScenarioId() {
		return pickedScenarioId;
	}

	public void setPickedScenarioId(String pickedScenarioId) {
		this.pickedScenarioId = pickedScenarioId;
	}
}
