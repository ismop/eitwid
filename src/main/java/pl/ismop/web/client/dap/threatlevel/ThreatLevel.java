package pl.ismop.web.client.dap.threatlevel;

import java.util.List;

import org.fusesource.restygwt.client.Json;

public class ThreatLevel {
	@Json(name = "profile_id")
	private Long profileId;

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
