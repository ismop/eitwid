package pl.ismop.web.client.dap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Singleton;

import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.context.ContextService;
import pl.ismop.web.client.dap.context.ContextsResponse;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.device.DeviceService;
import pl.ismop.web.client.dap.device.DevicesResponse;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateResponse;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateService;
import pl.ismop.web.client.dap.experiment.ExperimentService;
import pl.ismop.web.client.dap.experiment.ExperimentsResponse;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeResponse;
import pl.ismop.web.client.dap.levee.LeveeService;
import pl.ismop.web.client.dap.levee.LeveesResponse;
import pl.ismop.web.client.dap.levee.ModeChange;
import pl.ismop.web.client.dap.levee.ModeChangeRequest;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.measurement.MeasurementService;
import pl.ismop.web.client.dap.measurement.MeasurementsResponse;
import pl.ismop.web.client.dap.monitoring.MonitoringResponse;
import pl.ismop.web.client.dap.monitoring.MonitoringService;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.parameter.ParameterService;
import pl.ismop.web.client.dap.parameter.ParametersResponse;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.profile.ProfileService;
import pl.ismop.web.client.dap.profile.ProfilesResponse;
import pl.ismop.web.client.dap.result.Result;
import pl.ismop.web.client.dap.result.ResultService;
import pl.ismop.web.client.dap.result.ResultsResponse;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.dap.scenario.ScenarioService;
import pl.ismop.web.client.dap.scenario.ScenariosResponse;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.section.SectionService;
import pl.ismop.web.client.dap.section.SectionsResponse;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.dap.sensor.SensorResponse;
import pl.ismop.web.client.dap.sensor.SensorService;
import pl.ismop.web.client.dap.sensor.SensorsResponse;
import pl.ismop.web.client.dap.threatassessment.ThreatAssessmentResponse;
import pl.ismop.web.client.dap.threatassessment.ThreatAssessmentService;
import pl.ismop.web.client.dap.threatlevel.ThreatLevel;
import pl.ismop.web.client.dap.threatlevel.ThreatLevelResponse;
import pl.ismop.web.client.dap.threatlevel.ThreatLevelService;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.dap.timeline.TimelineService;
import pl.ismop.web.client.dap.timeline.TimelinesResponse;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.error.ErrorUtil;
import pl.ismop.web.client.hypgen.Experiment;

@Singleton
public class DapController {

	private final IsmopConverter converter;

	private final ScenarioService scenarioService;

	private ExperimentService leveeExperimentService;

	private ErrorUtil errorUtil;

	private LeveeService leveeService;

	private SensorService sensorService;

	private MeasurementService measurementService;

	private SectionService sectionService;

	private ThreatAssessmentService threatAssessmentService;

	private ResultService resultService;

	private ProfileService profileService;

	private DeviceService deviceService;

	private DeviceAggregateService deviceAggregationService;

	private ParameterService parameterService;

	private ContextService contextService;

	private TimelineService timelineService;

	private MonitoringService monitoringService;

	private ThreatLevelService threatLevelService;

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

	public interface ThreatAssessmentCallback extends ErrorCallback {
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

	public interface DeviceAggregatesCallback extends ErrorCallback {
		void processDeviceAggregations(List<DeviceAggregate> deviceAggreagations);
	}

	public interface ExperimentsCallback extends ErrorCallback {
		void processExperiments(List<pl.ismop.web.client.dap.experiment.Experiment> experiments);
	}

	public interface ScenariosCallback extends ErrorCallback {
		void processScenarios(List<Scenario> scenarios);
	}

	public interface MalfunctioningParametersCallback extends ErrorCallback {
		void processMalfunctioningParameters(List<Parameter> malfunctioningParameters);
	}

	private class MeasurementsRestCallback implements MethodCallback<MeasurementsResponse> {
		private final MeasurementsCallback callback;

		public MeasurementsRestCallback(MeasurementsCallback callback) {
			this.callback = callback;
		}

		@Override
		public void onFailure(Method method, Throwable exception) {
			callback.onError(errorUtil.processErrors(method, exception));
		}

		@Override
		public void onSuccess(Method method, MeasurementsResponse response) {
			callback.processMeasurements(response.getMeasurements());
		}
	}

	@Inject
	public DapController(
			ErrorUtil errorUtil,
			LeveeService leveeService,
			SensorService sensorService,
			MeasurementService measurementService,
			SectionService sectionService,
			ThreatAssessmentService experimentService,
			ThreatLevelService threatLevelService,
			ResultService resultService,
			ProfileService profileService,
			DeviceService deviceService,
			DeviceAggregateService deviceAggregationService,
			ParameterService parameterService,
			ContextService contextService,
			TimelineService timelineService,
			ExperimentService leveeExperimentService,
			ScenarioService scenarioService,
			MonitoringService monitoringService,
			IsmopConverter converter) {
		this.errorUtil = errorUtil;
		this.leveeService = leveeService;
		this.sensorService = sensorService;
		this.measurementService = measurementService;
		this.sectionService = sectionService;
		this.threatAssessmentService = experimentService;
		this.threatLevelService = threatLevelService;
		this.resultService = resultService;
		this.profileService = profileService;
		this.deviceService = deviceService;
		this.deviceAggregationService = deviceAggregationService;
		this.parameterService = parameterService;
		this.contextService = contextService;
		this.timelineService = timelineService;
		this.leveeExperimentService = leveeExperimentService;
		this.scenarioService = scenarioService;
		this.monitoringService = monitoringService;
		this.converter = converter;
	}

	public void getLevees(final LeveesCallback callback) {
		leveeService.getLevees(new MethodCallback<LeveesResponse>() {
			@Override
			public void onSuccess(Method method, LeveesResponse response) {
				callback.processLevees(response.getLevees());
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
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
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, LeveeResponse response) {
				callback.processLevee(response.getLevee());
			}
		});
	}

	public void getLevee(String leveeId, final LeveeCallback callback) {
		leveeService.getLevee(leveeId, new MethodCallback<LeveeResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, LeveeResponse response) {
				callback.processLevee(response.getLevee());
			}
		});
	}

	public void getSensor(String sensorId, final SensorCallback callback) {
		sensorService.getSensor(sensorId, new MethodCallback<SensorResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, SensorResponse response) {
				callback.processSensor(response.getSensor());
			}
		});
	}

	public void getMeasurementsWithQuantity(String timelineId, int quantity,
			MeasurementsCallback callback) {
		measurementService.getMeasurementsWithQuantity(timelineId, quantity,
				new MeasurementsRestCallback(callback));
	}

	public void getMeasurements(String timelineId, Date startDate, Date endDate,
			MeasurementsCallback callback) {
		String until = converter.formatForDto(endDate);
		String from = converter.formatForDto(startDate);
		measurementService.getMeasurements(timelineId, from, until,
				new MeasurementsRestCallback(callback));
	}

	public void getMeasurementsWithQuantityAndTime(List<String> timelineIds, Date startDate, Date endDate,
			int quantity, MeasurementsCallback callback) {
		String from = converter.formatForDto(startDate);
		String until = converter.formatForDto(endDate);
		measurementService.getMeasurementsWithQuantityAndTime(converter.merge(timelineIds), from,
				until, quantity, new MeasurementsRestCallback(callback));
	}

	public ListenableFuture<List<Measurement>> getMeasurementsWithQuantityAndTime(
			List<String> timelineIds, Date startDate, Date endDate, int quantity) {
		SettableFuture<List<Measurement>> result = SettableFuture.create();
		String from = converter.formatForDto(startDate);
		String until = converter.formatForDto(endDate);
		measurementService.getMeasurementsWithQuantityAndTime(converter.merge(timelineIds), from,
				until, quantity, new MethodCallback<MeasurementsResponse>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						result.setException(errorUtil.processErrorsForException(method, exception));
					}

					@Override
					public void onSuccess(Method method, MeasurementsResponse response) {
						result.set(response.getMeasurements());
					}});

		return result;
	}

	public void getMeasurements(Collection<String> timelineIds, Date startDate, Date endDate,
			MeasurementsCallback callback) {
		getMeasurements(converter.merge(timelineIds), startDate, endDate, callback);
	}

	public void getAllMeasurements(Collection<String> timelineIds, final MeasurementsCallback callback) {
		measurementService.getAllMeasurements(converter.merge(timelineIds), new MethodCallback<MeasurementsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, MeasurementsResponse measurementsResponse) {
				callback.processMeasurements(measurementsResponse.getMeasurements());
			}
		});
	}

	public void getLastMeasurementsWith24HourMod(List<String> timelineIds, Date untilDate, final MeasurementsCallback callback) {
		String until = converter.formatForDto(untilDate);
		String from = converter.formatForDto(new Date(untilDate.getTime() - 86_400_000L));
		measurementService.getLastMeasurements(converter.merge(timelineIds), from, until,
				new MeasurementsRestCallback(callback));
	}

	public ListenableFuture<List<Measurement>> getLastMeasurementsWith24HourMod(
			List<String> timelineIds, Date untilDate) {
		SettableFuture<List<Measurement>> result = SettableFuture.create();
		String until = converter.formatForDto(untilDate);
		String from = converter.formatForDto(new Date(untilDate.getTime() - 86_400_000L));
		measurementService.getLastMeasurements(converter.merge(timelineIds, ","), from, until,
				new MethodCallback<MeasurementsResponse>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						result.setException(errorUtil.processErrorsForException(method, exception));
					}

					@Override
					public void onSuccess(Method method, MeasurementsResponse response) {
						result.set(response.getMeasurements());
					}
				});

		return result;
	}

	public void getLastMeasurements(Collection<String> timelineIds, Date untilDate, final MeasurementsCallback callback) {
		String until = converter.formatForDto(untilDate);
		measurementService.getLastMeasurementsOnlyUntil(converter.merge(timelineIds), until,
				new MeasurementsRestCallback(callback));
	}

	public ListenableFuture<List<Measurement>> getLastMeasurements(List<String> timelineIds,
			Date untilDate) {
		SettableFuture<List<Measurement>> result = SettableFuture.create();
		String until = converter.formatForDto(untilDate);
		measurementService.getLastMeasurementsOnlyUntil(converter.merge(timelineIds, ","), until,
				new MethodCallback<MeasurementsResponse>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						result.setException(errorUtil.processErrorsForException(method, exception));
					}

					@Override
					public void onSuccess(Method method, MeasurementsResponse response) {
						result.set(response.getMeasurements());
					}
				});

		return result;
	}

	public void getSections(float top, float left, float bottom, float right, final SectionsCallback callback) {
		sectionService.getSections(converter.createSelectionQuery(top, left, bottom, right),
				new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				callback.processSections(response.getSections());
			}
		});
	}

	public void getSections(List<String> sectionIds, final SectionsCallback callback) {
		sectionService.getSectionsById(converter.merge(sectionIds), new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
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
				sectionsCallback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				sectionsCallback.processSections(response.getSections());
			}
		});
	}

	public ListenableFuture<List<Section>> getSections() {
		SettableFuture<List<Section>> result = SettableFuture.create();
		sectionService.getSections(new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				result.set(response.getSections());
			}
		});

		return result;
	}

	public void getSections(String leveeId, final SectionsCallback sectionsCallback) {
		sectionService.getSectionsForLevee(leveeId, new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				sectionsCallback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				sectionsCallback.processSections(response.getSections());
			}
		});
	}

	public void getExperiments(List<String> experimentIds,
			final ThreatAssessmentCallback callback) {
		threatAssessmentService.getExperiments(converter.merge(experimentIds),
				new MethodCallback<ThreatAssessmentResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
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
				callback.onError(errorUtil.processErrors(method, exception));
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
				callback.onError(errorUtil.processErrors(method, exception));
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
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, ProfilesResponse response) {
				callback.processProfiles(response.getProfiles());
			}
		});
	}

	public void getProfiles(List<String> sectionIds, final ProfilesCallback callback) {
		profileService.getProfilesForSection(converter.merge(sectionIds), new MethodCallback<ProfilesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, ProfilesResponse response) {
				callback.processProfiles(response.getProfiles());
			}
		});
	}

	public ListenableFuture<List<Profile>> getProfiles(List<String> sectionIds) {
		SettableFuture<List<Profile>> result = SettableFuture.create();
		profileService.getProfilesForSection(converter.merge(sectionIds), new MethodCallback<ProfilesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, ProfilesResponse response) {
				result.set(response.getProfiles());
			}
		});

		return result;
	}

	public void getDevicesRecursively(String profileId, final DevicesCallback callback) {
		deviceAggregationService.getDeviceAggregates(profileId, new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				SettableFuture<Void> futureCallback = SettableFuture.create();
				List<Device> devices = new ArrayList<>();
				collectDevices(response.getDeviceAggregations(), devices,
						new MutableInteger(0), futureCallback);
				Futures.addCallback(futureCallback, new FutureCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						callback.processDevices(devices);
					}

					@Override
					public void onFailure(Throwable t) {
						callback.onError(new ErrorDetails(t.getMessage()));
					}
				});
			}
		});
	}

	public void getParameters(String deviceIdFilter, final ParametersCallback callback) {
		parameterService.getParameters(deviceIdFilter, new MethodCallback<ParametersResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, ParametersResponse response) {
				callback.processParameters(response.getParameters());
			}
		});
	}

	public void getParameters(List<String> deviceIds, final ParametersCallback callback) {
		parameterService.getParameters(converter.merge(deviceIds), new MethodCallback<ParametersResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, ParametersResponse response) {
				callback.processParameters(response.getParameters());
			}
		});
	}

	public ListenableFuture<List<Parameter>> getParameters(List<String> deviceIds) {
		SettableFuture<List<Parameter>> result = SettableFuture.create();
		parameterService.getParameters(converter.merge(deviceIds, ","), new MethodCallback<ParametersResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, ParametersResponse response) {
				result.set(response.getParameters());
			}
		});

		return result;
	}

	public void getParametersById(Collection<String> ids, final ParametersCallback callback) {
		parameterService.getParametersById(converter.merge(ids), new MethodCallback<ParametersResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, ParametersResponse response) {
				callback.processParameters(response.getParameters());
			}
		});
	}

	public void getLeveeParameters(Integer leveeId, final ParametersCallback callback) {
		parameterService.getLeveeParameters(leveeId, new MethodCallback<ParametersResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
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
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, ContextsResponse response) {
				callback.processContexts(response.getContexts());
			}
		});
	}

	public ListenableFuture<List<Context>> getContext(String contextType) {
		SettableFuture<List<Context>> result = SettableFuture.create();
		contextService.getContexts(contextType, new MethodCallback<ContextsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, ContextsResponse response) {
				result.set(response.getContexts());
			}
		});

		return result;
	}

	public void getContexts(List<String> contextIds, final ContextsCallback callback) {
		contextService.getContextsById(converter.merge(contextIds), new MethodCallback<ContextsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
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
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, TimelinesResponse response) {
				callback.processTimelines(response.getTimelines());
			}
		});
	}

	public void getTimelinesForParameterIds(String contextId, Collection<String> parameterIds, final TimelinesCallback callback) {
		timelineService.getTimelines(contextId, converter.merge(parameterIds),
				new MethodCallback<TimelinesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, TimelinesResponse response) {
				callback.processTimelines(response.getTimelines());
			}
		});
	}

	public ListenableFuture<List<Timeline>> getTimelinesForParameterIds(String contextId,
			List<String> parameterIds) {
		SettableFuture<List<Timeline>> result = SettableFuture.create();
		timelineService.getTimelines(contextId, converter.merge(parameterIds, ","),
				new MethodCallback<TimelinesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, TimelinesResponse response) {
				result.set(response.getTimelines());
			}
		});

		return result;
	}

	public void getParameterTimelines(String parameterId, final TimelinesCallback callback) {
		timelineService.getParameterTimelines(parameterId, new MethodCallback<TimelinesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, TimelinesResponse response) {
				callback.processTimelines(response.getTimelines());
			}
		});
	}

	public void getExperimentTimelines(String experimentId, final TimelinesCallback callback) {
		timelineService.getExperimentTimelines(experimentId, new MethodCallback<TimelinesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, TimelinesResponse response) {
				callback.processTimelines(response.getTimelines());
			}
		});
	}

	public void getMeasurementsForTimelineIds(Collection<String> timelineIds, final MeasurementsCallback callback) {
		String until = converter.formatForDto(new Date());
		String from = converter.formatForDto(monthEarlier());
		measurementService.getMeasurements(converter.merge(timelineIds), from, until,
				new MethodCallback<MeasurementsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, MeasurementsResponse response) {
				callback.processMeasurements(response.getMeasurements());
			}
		});
	}

	public void getMeasurementsForTimelineIdsWithQuantity(List<String> timelineIds, int quantity, final MeasurementsCallback callback) {
		measurementService.getMeasurementsWithQuantity(converter.merge(timelineIds),
				quantity, new MethodCallback<MeasurementsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, MeasurementsResponse response) {
				callback.processMeasurements(response.getMeasurements());
			}
		});
	}

	public void getDeviceAggregations(String profileId, final DeviceAggregatesCallback callback) {
		deviceAggregationService.getDeviceAggregates(profileId, new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}

	public ListenableFuture<List<DeviceAggregate>> getDeviceAggregations(String profileId) {
		SettableFuture<List<DeviceAggregate>> result = SettableFuture.create();
		deviceAggregationService.getDeviceAggregates(profileId,
				new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				result.set(response.getDeviceAggregations());
			}
		});

		return result;
	}

	public void getDeviceAggregations(List<String> profileIds, final DeviceAggregatesCallback callback) {
		deviceAggregationService.getDeviceAggregates(converter.merge(profileIds),
				new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}

	public ListenableFuture<List<DeviceAggregate>> getDeviceAggregations(List<String> profileIds) {
		SettableFuture<List<DeviceAggregate>> result = SettableFuture.create();
		deviceAggregationService.getDeviceAggregates(converter.merge(profileIds),
				new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				result.set(response.getDeviceAggregations());
			}
		});

		return result;
	}

	public void getDeviceAggregationsForSectionId(String sectionIdFilter, final DeviceAggregatesCallback callback) {
		deviceAggregationService.getDeviceAggregatesForSectionIds(sectionIdFilter, new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}

	public void getDevicesRecursivelyForAggregate(String aggregateId, final DevicesCallback callback) {
		deviceAggregationService.getDeviceAggregatesForIds(aggregateId, new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				SettableFuture<Void> futureCallback = SettableFuture.create();
				List<Device> devices = new ArrayList<>();
				collectDevices(response.getDeviceAggregations(), devices,
						new MutableInteger(0), futureCallback);
				Futures.addCallback(futureCallback, new FutureCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						callback.processDevices(devices);
					}

					@Override
					public void onFailure(Throwable t) {
						callback.onError(new ErrorDetails(t.getMessage()));
					}
				});
			}
		});
	}

	public void getDevicesRecursivelyForAggregates(List<String> deviceAggregationIds, final DevicesCallback callback) {
		deviceAggregationService.getDeviceAggregatesForIds(converter.merge(deviceAggregationIds),
				new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				SettableFuture<Void> futureCallback = SettableFuture.create();
				List<Device> devices = new ArrayList<>();
				collectDevices(response.getDeviceAggregations(), devices,
						new MutableInteger(0), futureCallback);
				Futures.addCallback(futureCallback, new FutureCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						callback.processDevices(devices);
					}

					@Override
					public void onFailure(Throwable t) {
						callback.onError(new ErrorDetails(t.getMessage()));
					}
				});
			}
		});
	}

	public ListenableFuture<List<Device>> getDevicesRecursivelyForAggregates(
			List<String> deviceAggregationIds) {
		SettableFuture<List<Device>> result = SettableFuture.create();
		deviceAggregationService.getDeviceAggregatesForIds(converter.merge(deviceAggregationIds),
				new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				SettableFuture<Void> futureCallback = SettableFuture.create();
				List<Device> devices = new ArrayList<>();
				collectDevices(response.getDeviceAggregations(), devices,
						new MutableInteger(0), futureCallback);
				Futures.addCallback(futureCallback, new FutureCallback<Void>() {
					@Override
					public void onSuccess(Void v) {
						result.set(devices);
					}

					@Override
					public void onFailure(Throwable t) {
						result.setException(t);
					}
				});
			}
		});

		return result;
	}

	public void getDeviceAggregationForType(String type, final DeviceAggregatesCallback callback) {
		deviceAggregationService.getDeviceAggregatesForType(type, new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}

	public void getDeviceAggregationForType(String type, String leveeId, final DeviceAggregatesCallback callback) {
		deviceAggregationService.getDeviceAggregatesForType(type, leveeId, new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}

	public void getDevicesForType(String deviceType, final DevicesCallback callback) {
		deviceService.getDevicesForType(deviceType, new MethodCallback<DevicesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DevicesResponse response) {
				callback.processDevices(response.getDevices());
			}
		});
	}

	public ListenableFuture<List<Device>> getDevicesForType(String deviceType) {
		SettableFuture<List<Device>> result = SettableFuture.create();
		deviceService.getDevicesForType(deviceType, new MethodCallback<DevicesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}

			@Override
			public void onSuccess(Method method, DevicesResponse response) {
				result.set(response.getDevices());
			}
		});

		return result;
	}

	public void getDevicesForType(Collection<String> deviceType, final DevicesCallback callback) {
		getDevicesForType(converter.merge(deviceType), callback);
	}

	public void getDevicesForSection(String sectionId, final DevicesCallback callback) {
		deviceService.getDevicesForSectionId(sectionId, new MethodCallback<DevicesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DevicesResponse response) {
				callback.processDevices(response.getDevices());
			}
		});
	}

	public void getDevicesForSectionAndType(String sectionId, String deviceType, final DevicesCallback callback) {
		deviceService.getDevicesForSectionIdAndType(sectionId, deviceType, new MethodCallback<DevicesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DevicesResponse response) {
				callback.processDevices(response.getDevices());
			}
		});
	}

	public void getDevices(List<String> deviceIds, final DevicesCallback callback) {
		deviceService.getDevicesForIds(converter.merge(deviceIds), new MethodCallback<DevicesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DevicesResponse response) {
				callback.processDevices(response.getDevices());
			}
		});
	}

	public void getLeveeDevices(Integer leveeId, final DevicesCallback callback) {
		deviceService.getLeveeDevices(leveeId, new MethodCallback<DevicesResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DevicesResponse response) {
				callback.processDevices(response.getDevices());
			}
		});
	}

	public void getDeviceAggregation(String deviceAggregationId, final DeviceAggregatesCallback callback) {
		deviceAggregationService.getDeviceAggregatesForIds(deviceAggregationId, new MethodCallback<DeviceAggregateResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, DeviceAggregateResponse response) {
				callback.processDeviceAggregations(response.getDeviceAggregations());
			}
		});
	}

	public void getExperiments(final ExperimentsCallback callback) {
		leveeExperimentService.getExperiments(new MethodCallback<ExperimentsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, ExperimentsResponse experiments) {
				callback.processExperiments(experiments.getExperiments());
			}
		});
	}

	public void getExperimentScenarios(String experimentId, final ScenariosCallback callback) {
		scenarioService.getExperimentScenarios(experimentId, new MethodCallback<ScenariosResponse>() {
			@Override
			public void onSuccess(Method method, ScenariosResponse scenarios) {
				callback.processScenarios(scenarios.getScenarios());
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(errorUtil.processErrors(method, exception));
			}
		});
	}

	public void getMalfunctioningParameters(final MalfunctioningParametersCallback malfunctioningParametersCallback) {
		monitoringService.getMonitoringInfo(new MethodCallback<MonitoringResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				malfunctioningParametersCallback.onError(errorUtil.processErrors(method, exception));
			}

			@Override
			public void onSuccess(Method method, MonitoringResponse response) {
				malfunctioningParametersCallback.processMalfunctioningParameters(response.getParameters());
			}
		});
	}

	public ListenableFuture<List<Device>> getDevicesWithCustomIds(List<String> customIds) {
		SettableFuture<List<Device>> result = SettableFuture.create();
		deviceService.getDevicesFotCustomIds(converter.merge(customIds),
				new MethodCallback<DevicesResponse>() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						result.setException(errorUtil.processErrorsForException(method, exception));
					}

					@Override
					public void onSuccess(Method method, DevicesResponse response) {
						result.set(response.getDevices());
					}
				});

		return result;
	}

	public ListenableFuture<List<ThreatLevel>> getThreatLevels(int limit, Date from, Date to, String status) {
		SettableFuture<List<ThreatLevel>> result = SettableFuture.create();

		String fromStr = converter.formatForDto(from);
        String toStr = converter.formatForDto(to);

        threatLevelService.getThreatLevels(limit, fromStr, toStr, status, new MethodCallback<ThreatLevelResponse>() {

			@Override
			public void onSuccess(Method method, ThreatLevelResponse response) {
				result.set(response.getThreatLevels());
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				result.setException(errorUtil.processErrorsForException(method, exception));
			}
		});

		return result;
	}

	private Date monthEarlier() {
		return new Date(new Date().getTime() - 2678400000L);
	}

	private void collectDevices(List<DeviceAggregate> deviceAggregations ,List<Device> result,
			MutableInteger requestCounter, SettableFuture<Void> futureCallback) {
		if(deviceAggregations.size() > 0) {
			for(DeviceAggregate deviceAggregation : deviceAggregations) {
				requestCounter.increment();
				deviceService.getDevices(deviceAggregation.getId(),
						new MethodCallback<DevicesResponse>() {
							@Override
							public void onFailure(Method method, Throwable exception) {
								requestCounter.decrement();

								if(requestCounter.get() == 0) {
									futureCallback.setException(errorUtil.processErrorsForException(
											method, exception));
								}
							}

							@Override
							public void onSuccess(Method method, DevicesResponse response) {
								result.addAll(response.getDevices());
								requestCounter.decrement();

								if(requestCounter.get() == 0) {
									futureCallback.set(null);
								}
							}
						});

				if(deviceAggregation.getChildrenIds() != null && deviceAggregation.getChildrenIds().size() > 0) {
					requestCounter.increment();
					deviceAggregationService.getDeviceAggregatesForIds(converter.merge(deviceAggregation.getChildrenIds()),
							new MethodCallback<DeviceAggregateResponse>() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							requestCounter.decrement();

							if(requestCounter.get() == 0) {
								futureCallback.setException(errorUtil.processErrorsForException(
										method, exception));
							}
						}

						@Override
						public void onSuccess(Method method, DeviceAggregateResponse response) {
							requestCounter.decrement();
							collectDevices(response.getDeviceAggregations(), result, requestCounter,
									futureCallback);

							if(requestCounter.get() == 0) {
								futureCallback.set(null);
							}
						}
					});
				}
			}
		} else {
			futureCallback.set(null);
		}
	}
}
