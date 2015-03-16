package pl.ismop.web.client.dap.threatassessment;

import java.util.List;

import org.fusesource.restygwt.client.Json;

import pl.ismop.web.client.hypgen.Experiment;

public class ThreatAssessmentResponse {
	@Json(name = "threat_assessments")
	private List<Experiment> experiments;

	public List<Experiment> getExperiments() {
		return experiments;
	}

	public void setExperiments(List<Experiment> experiments) {
		this.experiments = experiments;
	}
}