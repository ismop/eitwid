package pl.ismop.web.client.dap.measurement;

import java.util.List;

public class MeasurementsResponse {
	private List<Measurement> measurements;

	public List<Measurement> getMeasurements() {
		return measurements;
	}
	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}
}