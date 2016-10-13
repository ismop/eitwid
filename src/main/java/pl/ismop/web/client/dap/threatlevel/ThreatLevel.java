package pl.ismop.web.client.dap.threatlevel;

import java.util.List;

import org.fusesource.restygwt.client.Json;

public class ThreatLevel {
	@Json(name = "profile_id")
	private Long profileId;

	@Json(name = "profile_custom_id")
	private String profileCustomId;

	@Json(name = "section_id")
	private String sectionId;

	@Json(name = "threat_assessments")
	private List<ThreatAssessment> threatAssessments;

	@Json(name = "threat_level_assessment_runs")
	private List<ThreatLevelAssessmentRun> threatLevelAssessmentRuns;

	public Long getProfileId() {
		return profileId;
	}

	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}

	public String getProfileCustomId() {
		return profileCustomId;
	}

	public void setProfileCustomId(String profileCustomId) {
		this.profileCustomId = profileCustomId;
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public List<ThreatAssessment> getThreatAssessments() {
		return threatAssessments;
	}

	public void setThreatAssessments(List<ThreatAssessment> threatAssessments) {
		this.threatAssessments = threatAssessments;
	}

	public List<ThreatLevelAssessmentRun> getThreatLevelAssessmentRuns() {
		return threatLevelAssessmentRuns;
	}

	public void setThreatLevelAssessmentRuns(List<ThreatLevelAssessmentRun> threatLevelAssessmentRuns) {
		this.threatLevelAssessmentRuns = threatLevelAssessmentRuns;
	}
}
