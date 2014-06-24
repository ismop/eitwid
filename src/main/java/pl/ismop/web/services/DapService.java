package pl.ismop.web.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeServiceSync;
import pl.ismop.web.client.dap.levee.LeveesResponse;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.dap.sensor.SensorServiceSync;

@Service
public class DapService {
	private LeveeServiceSync leveeService;
	private SensorServiceSync sensorService;

	@Autowired
	public DapService(LeveeServiceSync leveeService, SensorServiceSync sensorService) {
		this.leveeService = leveeService;
		this.sensorService = sensorService;
	}
	
	public List<Levee> getLevees() {
		LeveesResponse response = leveeService.getLevees();
		
		return response.getLevees();
	}

	public List<Sensor> getSensors() {
		return sensorService.getSensors().getSensors();
	}
}