package pl.ismop.web.client.dap.experiment;

import java.util.List;

import pl.ismop.web.client.hypgen.Experiment;

public class ExperimentsResponse {
	private List<Experiment> experiments;

	public List<Experiment> getExperiments() {
		return experiments;
	}

	public void setExperiments(List<Experiment> experiments) {
		this.experiments = experiments;
	}
}