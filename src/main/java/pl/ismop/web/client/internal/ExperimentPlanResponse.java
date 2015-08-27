package pl.ismop.web.client.internal;

import org.fusesource.restygwt.client.Json;

public class ExperimentPlanResponse {
	@Json(name = "_embedded")
	private ExperimentPlanList embedded;

	public ExperimentPlanList getEmbedded() {
		return embedded;
	}

	public void setEmbedded(ExperimentPlanList embedded) {
		this.embedded = embedded;
	}
}