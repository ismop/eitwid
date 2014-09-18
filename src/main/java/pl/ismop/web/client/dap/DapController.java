package pl.ismop.web.client.dap;

import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeResponse;
import pl.ismop.web.client.dap.levee.LeveeService;
import pl.ismop.web.client.dap.levee.LeveesResponse;
import pl.ismop.web.client.dap.levee.ModeChange;
import pl.ismop.web.client.dap.levee.ModeChangeRequest;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.measurement.MeasurementService;
import pl.ismop.web.client.dap.measurement.MeasurementsResponse;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.profile.ProfileService;
import pl.ismop.web.client.dap.profile.ProfilesResponse;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.dap.sensor.SensorResponse;
import pl.ismop.web.client.dap.sensor.SensorService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DapController {
	private LeveeService leveeService;
	private SensorService sensorService;
	private MeasurementService measurementService;
	private ProfileService profileService;
	
	public interface ErrorCallback {
		void onError(int code, String message);
	}
	
	public interface LeveesCallback extends ErrorCallback {
		void processLevees(List<Levee> levees);
	}
	
	public interface LeveeCallback extends ErrorCallback {
		void processLevee(Levee levee);
	}
	
	public interface SensorCallback extends ErrorCallback {
		void processSensor(Sensor sensor);
	}
	
	public interface MeasurementsCallback extends ErrorCallback {
		void processMeasurements(List<Measurement> measurements);
	}
	
	public interface ProfilesCallback extends ErrorCallback {
		void processProfiles(List<Profile> profiles);
	}

	@Inject
	public DapController(LeveeService leveeService, SensorService sensorService,
			MeasurementService measurementService, ProfileService profileService) {
		this.leveeService = leveeService;
		this.sensorService = sensorService;
		this.measurementService = measurementService;
		this.profileService = profileService;
	}
	
	public void getLevees(final LeveesCallback callback) {	
		leveeService.getLevees(new MethodCallback<LeveesResponse>() {
			@Override
			public void onSuccess(Method method, LeveesResponse response) {
				callback.processLevees(response.getLevees());
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}
		});
	}

	public void changeLeveeMode(String leveeId, String mode, final LeveeCallback callback) {
		ModeChange modeChange = new ModeChange();
		modeChange.setMode(mode);
		
		ModeChangeRequest request = new ModeChangeRequest();
		request.setModeChange(modeChange);
		leveeService.changeMode(leveeId, request, new MethodCallback<LeveeResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, LeveeResponse response) {
				callback.processLevee(response.getLevee());
			}});
	}

	public void getLevee(String leveeId, final LeveeCallback callback) {
		leveeService.getLevee(leveeId, new MethodCallback<LeveeResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, LeveeResponse response) {
				callback.processLevee(response.getLevee());
			}});
	}

	public void getSensor(String sensorId, final SensorCallback callback) {
		sensorService.getSensor(sensorId, new MethodCallback<SensorResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SensorResponse response) {
				callback.processSensor(response.getSensor());
			}
		});
	}

	public void getMeasurements(String sensorId, final MeasurementsCallback callback) {
		measurementService.getMeasurements(sensorId, new MethodCallback<MeasurementsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, MeasurementsResponse response) {
				callback.processMeasurements(response.getMeasurements());
			}
		});
	}
	
	public void getProfiles(float top, float left, float bottom, float right, final ProfilesCallback callback) {
		profileService.getProfiles(createSelectionQuery(top, left, bottom, right), new MethodCallback<ProfilesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ProfilesResponse response) {
				callback.processProfiles(response.getProfiles());
			}
		});
	}

	private String createSelectionQuery(double top, double left, double bottom, double right) {
		StringBuilder builder = new StringBuilder();
		builder.append("POLYGON ((")
				.append(top).append(" ").append(left).append(", ")
				.append(top).append(" ").append(right).append(", ")
				.append(bottom).append(" ").append(right).append(", ")
				.append(bottom).append(" ").append(left).append(", ")
				.append(top).append(" ").append(left)
				.append("))");
		
		return builder.toString();
	}
}