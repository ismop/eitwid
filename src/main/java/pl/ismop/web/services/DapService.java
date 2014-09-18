package pl.ismop.web.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeServiceSync;
import pl.ismop.web.client.dap.levee.LeveesResponse;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.profile.ProfileServiceSync;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.dap.sensor.SensorServiceSync;

@Service
public class DapService {
	private LeveeServiceSync leveeService;
	private SensorServiceSync sensorService;
	private ProfileServiceSync profileService;

	@Autowired
	public DapService(LeveeServiceSync leveeService, SensorServiceSync sensorService, ProfileServiceSync profileService) {
		this.leveeService = leveeService;
		this.sensorService = sensorService;
		this.profileService = profileService;
	}
	
	public List<Levee> getLevees() {
		LeveesResponse response = leveeService.getLevees();
		
		return response.getLevees();
	}

	public List<Sensor> getSensors() {
		return sensorService.getSensors().getSensors();
	}

	public List<Profile> getProfiles() {
		return profileService.getProfiles().getProfiles();
	}
}