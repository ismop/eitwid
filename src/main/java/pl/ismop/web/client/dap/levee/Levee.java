package pl.ismop.web.client.dap.levee;

import org.fusesource.restygwt.client.Json;

public class Levee {
	private String id;
	private String name;
	@Json(name = "emergency_level")
	private String emergencyLevel;
	@Json(name = "threat_level")
	private String threatLevel;
	@Json(name = "threat_level_updated_at")
	private String threatLevelLastUpdate;
//	private String shape;
	
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
//	public String getShape() {
//		return shape;
//	}
//	public void setShape(String shape) {
//		this.shape = shape;
//	}
}