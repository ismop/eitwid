package pl.ismop.web.client.dap.result;

import org.fusesource.restygwt.client.Json;

public class Result {
	private String id;
	private float similarity;
	
	@Json(name = "section_id")
	private String sectionId;
	
	@Json(name = "threat_assessment_id")
	private String threatAssessmentId;
	
	@Json(name = "threat_level")
	private String threatLevel;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getSimilarity() {
		return similarity;
	}
	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}
	public String getSectionId() {
		return sectionId;
	}
	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}
	public String getThreatAssessmentId() {
		return threatAssessmentId;
	}
	public void setThreatAssessmentId(String threatAssessmentId) {
		this.threatAssessmentId = threatAssessmentId;
	}
	public String getThreatLevel() {
		return threatLevel;
	}
	public void setThreatLevel(String threatLevel) {
		this.threatLevel = threatLevel;
	}
}