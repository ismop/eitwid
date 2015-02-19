package pl.ismop.web.client.hypgen;

import org.fusesource.restygwt.client.Json;

public class ExperimentResponse {
	@Json(name = "threat_assessment")
	private Experiment experiment;

	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}
}