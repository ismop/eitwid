package pl.ismop.web.client.dap.levee;

import javax.xml.bind.annotation.XmlElement;

import org.fusesource.restygwt.client.Json;

public class Levee {
	private String id;
	private String name;
	@Json(name = "emergency_level")
	@XmlElement(name = "emergency_level")
	private String emergencyLevel;
	@Json(name = "threat_level")
	@XmlElement(name = "threat_level")
	private String threatLevel;
	@Json(name = "threat_level_updated_at")
	@XmlElement(name = "threat_level_updated_at")
	private String threatLevelLastUpdate;
	private Shape shape;
	
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
	public String getEmergencyLevel() {
		return emergencyLevel;
	}
	public void setEmergencyLevel(String emergencyLevel) {
		this.emergencyLevel = emergencyLevel;
	}
	public String getThreatLevel() {
		return threatLevel;
	}
	public void setThreatLevel(String threatLevel) {
		this.threatLevel = threatLevel;
	}
	public String getThreatLevelLastUpdate() {
		return threatLevelLastUpdate;
	}
	public void setThreatLevelLastUpdate(String threatLevelLastUpdate) {
		this.threatLevelLastUpdate = threatLevelLastUpdate;
	}
	public Shape getShape() {
		return shape;
	}
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	@Override
	public String toString() {
		return "Levee [id=" + id + ", name=" + name + ", emergencyLevel="
				+ emergencyLevel + ", threatLevel=" + threatLevel
				+ ", threatLevelLastUpdate=" + threatLevelLastUpdate
				+ ", shape=" + shape + "]";
	}
}