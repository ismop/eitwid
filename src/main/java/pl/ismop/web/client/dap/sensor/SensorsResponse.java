package pl.ismop.web.client.dap.sensor;

import java.util.List;

public class SensorsResponse {
	private List<Sensor> sensors;

	public List<Sensor> getSensors() {
		return sensors;
	}
	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}
}