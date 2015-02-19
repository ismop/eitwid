package pl.ismop.web.client.dap.section;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.fusesource.restygwt.client.Json;

import pl.ismop.web.client.dap.levee.Shape;

public class Section {
	private String id;
	private String name;
	@Json(name = "experiment_ids")
	@XmlElement(name = "experiment_ids")
	private List<String> experimentIds;
	@Json(name = "sensor_ids")
	@XmlElement(name = "sensor_ids")
	private List<String> sensorIds;
	private Shape shape;
	@Json(name = "threat_level")
	@XmlElement(name = "threat_level")
	private String threatLevel;
	@Json(name = "levee_id")
	@XmlElement(name = "levee_id")
	private String leveeId;
	@Json(name = "profile_shape")
	@XmlElement(name = "profile_shape")
	private Shape profileShape;
	
	public String getLeveeId() {
		return leveeId;
	}
	public void setLeveeId(String leveeId) {
		this.leveeId = leveeId;
	}
	public Shape getProfileShape() {
		return profileShape;
	}
	public void setProfileShape(Shape profileShape) {
		this.profileShape = profileShape;
	}
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
	public Shape getShape() {
		return shape;
	}
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	@Override
	public String toString() {
		return "Profile [id=" + id + ", name=" + name + ", experimentIds=" + experimentIds + ", sensorIds=" + sensorIds + ", shape=" + shape + "]";
	}
	public String getThreatLevel() {
		return threatLevel;
	}
	public void setThreatLevel(String threatLevel) {
		this.threatLevel = threatLevel;
	}
}