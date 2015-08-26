package pl.ismop.web.client.dap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.inject.Singleton;

import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.context.ContextService;
import pl.ismop.web.client.dap.context.ContextsResponse;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.device.DeviceService;
import pl.ismop.web.client.dap.device.DevicesResponse;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregationService;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregationsResponse;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeResponse;
import pl.ismop.web.client.dap.levee.LeveeService;
import pl.ismop.web.client.dap.levee.LeveesResponse;
import pl.ismop.web.client.dap.levee.ModeChange;
import pl.ismop.web.client.dap.levee.ModeChangeRequest;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.measurement.MeasurementService;
import pl.ismop.web.client.dap.measurement.MeasurementsResponse;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.parameter.ParameterService;
import pl.ismop.web.client.dap.parameter.ParametersResponse;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.profile.ProfileService;
import pl.ismop.web.client.dap.profile.ProfilesResponse;
import pl.ismop.web.client.dap.result.Result;
import pl.ismop.web.client.dap.result.ResultService;
import pl.ismop.web.client.dap.result.ResultsResponse;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.section.SectionService;
import pl.ismop.web.client.dap.section.SectionsResponse;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.dap.sensor.SensorResponse;
import pl.ismop.web.client.dap.sensor.SensorService;
import pl.ismop.web.client.dap.sensor.SensorsResponse;
import pl.ismop.web.client.dap.threatassessment.ThreatAssessmentResponse;
import pl.ismop.web.client.dap.threatassessment.ThreatAssessmentService;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.dap.timeline.TimelineService;
import pl.ismop.web.client.dap.timeline.TimelinesResponse;
import pl.ismop.web.client.hypgen.Experiment;

@Singleton
public class DapController {
	private LeveeService leveeService;
	private SensorService sensorService;
	private MeasurementService measurementService;
	private SectionService sectionService;
	private ThreatAssessmentService experimentService;
	private ResultService resultService;
	private ProfileService profileService;
	private DeviceService deviceService;
	private DeviceAggregationService deviceAggregationService;
	private ParameterService parameterService;
	private ContextService contextService;
	private TimelineService timelineService;
	
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
	
	public interface SectionsCallback extends ErrorCallback {
		void processSections(List<Section> sections);
	}
	
	public interface ExperimentsCallback extends ErrorCallback {
		void processExperiments(List<Experiment> experiments);
	}
	
	public interface ResultsCallback extends ErrorCallback {
		void processResults(List<Result> results);
	}
	
	public interface SensorsCallback extends ErrorCallback {
		void processSensors(List<Sensor> sensors);
	}
	
	public interface ProfilesCallback extends ErrorCallback {
		void processProfiles(List<Profile> profiles);
	}
	
	public interface DevicesCallback extends ErrorCallback {
		void processDevices(List<Device> devices);
	}
	
	public interface ParametersCallback extends ErrorCallback {
		void processParameters(List<Parameter> parameters);
	}
	
	public interface ContextsCallback extends ErrorCallback {
		void processContexts(List<Context> contexts);
	}
	
	public interface TimelinesCallback extends ErrorCallback {
		void processTimelines(List<Timeline> timelines);
	}
	
	public interface DeviceAggregationsCallback extends ErrorCallback {
		void processDeviceAggregations(List<DeviceAggregation> deviceAggreagations);
	}

	@Inject
	public DapController(LeveeService leveeService, SensorService sensorService, MeasurementService measurementService, SectionService sectionService,
			ThreatAssessmentService experimentService, ResultService resultService, ProfileService profileService, DeviceService deviceService,
			DeviceAggregationService deviceAggregationService, ParameterService parameterService, ContextService contextService,
			TimelineService timelineService) {
		this.leveeService = leveeService;
		this.sensorService = sensorService;
		this.measurementService = measurementService;
		this.sectionService = sectionService;
		this.experimentService = experimentService;
		this.resultService = resultService;
		this.profileService = profileService;
		this.deviceService = deviceService;
		this.deviceAggregationService = deviceAggregationService;
		this.parameterService = parameterService;
		this.contextService = contextService;
		this.timelineService = timelineService;
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

	public void getMeasurements(String timelineId, final MeasurementsCallback callback) {
		String until = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(new Date());
		String from = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(monthEarlier());
		measurementService.getMeasurements(timelineId, from, until, new MethodCallback<MeasurementsResponse>() {
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
	
	public void getSections(float top, float left, float bottom, float right, final SectionsCallback callback) {
		sectionService.getSections(createSelectionQuery(top, left, bottom, right), new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				callback.processSections(response.getSections());
			}
		});
	}
	
	public void getSections(final SectionsCallback sectionsCallback) {
		sectionService.getSections(new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				sectionsCallback.processSections(response.getSections());
			}
		});
	}
	
	public void getSections(String leveeId, final SectionsCallback sectionsCallback) {
		sectionService.getSectionsForLevee(leveeId, new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				sectionsCallback.processSections(response.getSections());
			}
		});
	}
	
	public void getExperiments(List<String> experimentIds, final ExperimentsCallback callback) {
		experimentService.getExperiments(merge(experimentIds, ","), new MethodCallback<ThreatAssessmentResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ThreatAssessmentResponse response) {
				callback.processExperiments(response.getExperiments());
			}
		});
	}
	
	public void getResults(String experimentId, final ResultsCallback callback) {
		resultService.getResults(experimentId, new MethodCallback<ResultsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ResultsResponse response) {
				callback.processResults(response.getResults());
			}
		});
	}
	
	public void getSensors(String sectionId, final SensorsCallback callback) {
		sensorService.getSensorsForSection(sectionId, new MethodCallback<SensorsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SensorsResponse response) {
				callback.processSensors(response.getSensors());
			}
		});
	}

	public void getProfiles(String sectionId, final ProfilesCallback callback) {
		profileService.getProfilesForSection(sectionId, new MethodCallback<ProfilesResponse>() {
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

	public void getDevicesRecursively(String profileId, final DevicesCallback callback) {
		final List<Device> result = new ArrayList<>();
		deviceAggregationService.getDeviceAggregations(profileId, new MethodCallback<DeviceAggregationsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, "Message: " + method.getResponse().getText());
			}

			@Override
			public void onSuccess(Method method, DeviceAggregationsResponse response) {
				collectDevices(response.getDeviceAggregations(), result, new MutableInteger(0), new DevicesCallback() {
					@Override
					public void onError(int code, String message) {
						callback.onError(code, message);
					}
					
					@Override
					public void processDevices(List<Device> devices) {
						callback.processDevices(devices);
					}
				});
			}
		});
	}

	public void getParameters(String deviceId, final ParametersCallback callback) {
		parameterService.getParameters(deviceId, new MethodCallback<ParametersResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ParametersResponse response) {
				callback.processParameters(response.getParameters());
			}
		});
	}

	public void getParameters(List<String> deviceIds, final ParametersCallback callback) {
		parameterService.getParameters(merge(deviceIds, ","), new MethodCallback<ParametersResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ParametersResponse response) {
				callback.processParameters(response.getParameters());
			}
		});
	}

	public void getContext(String contextType, final ContextsCallback callback) {
		contextService.getContexts(contextType, new MethodCallback<ContextsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ContextsResponse response) {
				callback.processContexts(response.getContexts());
			}
		});
	}

	public void getTimeline(String contextId, String paramterId, final TimelinesCallback callback) {
		timelineService.getTimelines(contextId, paramterId, new MethodCallback<TimelinesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, TimelinesResponse response) {
				callback.processTimelines(response.getTimelines());
			}
		});
	}

	public void getTimelinesForParameterIds(String contextId, List<String> parameterIds, final TimelinesCallback callback) {
		timelineService.getTimelines(contextId, merge(parameterIds, ","), new MethodCallback<TimelinesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, TimelinesResponse response) {
				callback.processTimelines(response.getTimelines());
			}
		});
	}

	public void getMeasurementsForTimelineIds(List<String> measurementIds, final MeasurementsCallback callback) {
		String until = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(new Date());
		String from = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(monthEarlier());
		measurementService.getMeasurements(merge(measurementIds, ","), from, until, new MethodCallback<MeasurementsResponse>() {
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

	public void getDeviceAggregations(String profileId, final DeviceAggregationsCallback callback) {
		deviceAggregationService.getDeviceAggregations(profileId, new MethodCallback<DeviceAggregationsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, DeviceAggregationsResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}
	
	public void getDevicesRecursivelyForAggregate(String aggregateId, final DevicesCallback callback) {
		deviceAggregationService.getDeviceAggregationsForIds(aggregateId, new MethodCallback<DeviceAggregationsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, DeviceAggregationsResponse response) {
				collectDevices(response.getDeviceAggregations(), new ArrayList<Device>(), new MutableInteger(0), callback);
			}
		});
	}

	public void getDeviceAggregationForType(String type, final DeviceAggregationsCallback callback) {
		deviceAggregationService.getDeviceAggregationsForType(type, new MethodCallback<DeviceAggregationsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, DeviceAggregationsResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}

	public void getDevicesForType(String deviceType, final DevicesCallback callback) {
		deviceService.getDevicesForType(deviceType, new MethodCallback<DevicesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, DevicesResponse response) {
				callback.processDevices(response.getDevices());
			}});
	}

	private Date monthEarlier() {
		return new Date(new Date().getTime() - 3600000L);
	}

	private void collectDevices(List<DeviceAggregation> deviceAggregations, final List<Device> result, final MutableInteger requestCounter,
			final DevicesCallback devicesCallback) {
		if(deviceAggregations.size() > 0) {
			for(DeviceAggregation deviceAggregation : deviceAggregations) {
				requestCounter.increment();
				deviceService.getDevices(deviceAggregation.getId(), new MethodCallback<DevicesResponse>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						requestCounter.decrement();
						
						if(requestCounter.get() == 0) {
							devicesCallback.onError(0, exception.getMessage());
						}
					}
	
					@Override
					public void onSuccess(Method method, DevicesResponse response) {
						result.addAll(response.getDevices());
						requestCounter.decrement();
						
						if(requestCounter.get() == 0) {
							devicesCallback.processDevices(result);
						}
					}
				});
				
				if(deviceAggregation.getChildernIds() != null && deviceAggregation.getChildernIds().size() > 0) {
					requestCounter.increment();
					deviceAggregationService.getDeviceAggregationsForIds(merge(deviceAggregation.getChildernIds(), ","),
							new MethodCallback<DeviceAggregationsResponse>() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							requestCounter.decrement();
							
							if(requestCounter.get() == 0) {
								devicesCallback.onError(0, exception.getMessage());
							}
						}
	
						@Override
						public void onSuccess(Method method, DeviceAggregationsResponse response) {
							requestCounter.decrement();
							collectDevices(response.getDeviceAggregations(), result, requestCounter, devicesCallback);
							
							if(requestCounter.get() == 0) {
								devicesCallback.processDevices(result);
							}
						}
					});
				}
			}
		} else {
			devicesCallback.processDevices(result);
		}
	}

	private String merge(List<String> chunks, String delimeter) {
		StringBuilder result = new StringBuilder();
		
		for(String chunk : chunks) {
			result.append(chunk).append(delimeter);
		}
		
		if(result.length() > 0) {
			result.delete(result.length() - delimeter.length(), result.length());
		}
		
		return result.toString();
	}

	private String createSelectionQuery(double top, double left, double bottom, double right) {
		StringBuilder builder = new StringBuilder();
		builder.append("POLYGON ((")
				.append(left).append(" ").append(top).append(", ")
				.append(right).append(" ").append(top).append(", ")
				.append(right).append(" ").append(bottom).append(", ")
				.append(left).append(" ").append(bottom).append(", ")
				.append(left).append(" ").append(top)
				.append("))");
		
		return builder.toString();
	}
}