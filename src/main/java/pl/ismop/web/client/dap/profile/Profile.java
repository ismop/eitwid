package pl.ismop.web.client.dap.profile;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.fusesource.restygwt.client.Json;

public class Profile {
	private String id;
	private String name;
	@Json(name = "experiment_ids")
	@XmlElement(name = "experiment_ids")
	private List<String> experimentIds;
	@Json(name = "sensor_ids")
	@XmlElement(name = "sensor_ids")
	private List<String> sensorIds;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getExperimentIds() {
		return experimentIds;
	}
	public void setExperimentIds(List<String> experimentIds) {
		this.experimentIds = experimentIds;
	}
	public List<String> getSensorIds() {
		return sensorIds;
	}
	public void setSensorIds(List<String> sensorIds) {
		this.sensorIds = sensorIds;
	}
	@Override
	public String toString() {
		return "Profile [id=" + id + ", name=" + name + ", experimentIds=" + experimentIds + ", sensorIds=" + sensorIds + "]";
	}
}