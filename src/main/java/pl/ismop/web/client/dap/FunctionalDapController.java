package pl.ismop.web.client.dap;

import java.util.Date;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.concurrent.Future;
import javaslang.concurrent.Promise;
import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.context.ContextDirectService;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.device.DeviceDirectService;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateDirectService;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.measurement.MeasurementDirectService;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.parameter.ParameterDirectService;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.profile.ProfileDirectService;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.dap.scenario.ScenarioDirectService;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.section.SectionDirectService;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.dap.timeline.TimelineDirectService;

@Singleton
public class FunctionalDapController {

	private DeviceDirectService deviceService;

	private ProfileDirectService profileService;

	private DeviceAggregateDirectService deviceAggregateService;

	private ParameterDirectService parameterService;

	private ScenarioDirectService scenarioService;

	private SectionDirectService sectionService;

	private ContextDirectService contextService;

	private TimelineDirectService timelineService;

	private MeasurementDirectService measurementService;

	private IsmopConverter ismopConverter;

	@Inject
	public FunctionalDapController(IsmopConverter ismopConverter,
			ProfileDirectService profileService,
			DeviceDirectService deviceService, DeviceAggregateDirectService deviceAggregateService,
			ParameterDirectService parameterService, ScenarioDirectService scenarioService,
			SectionDirectService sectionService, ContextDirectService contextService,
			TimelineDirectService timelineService, MeasurementDirectService measurementService) {
		this.ismopConverter = ismopConverter;
		this.profileService = profileService;
		this.deviceService = deviceService;
		this.deviceAggregateService = deviceAggregateService;
		this.parameterService = parameterService;
		this.scenarioService = scenarioService;
		this.sectionService = sectionService;
		this.contextService = contextService;
		this.timelineService = timelineService;
		this.measurementService = measurementService;
	}

	public Future<Seq<Device>> getAllDevicesForProfile(String profileId) {
		return collectDeviceAggregates(getDeviceAggregatesForProfile(profileId))
			.flatMap(aggregates -> getDevicesForAggregates(
					aggregates.map(DeviceAggregate::getId)));
	}

	public Future<Seq<Device>> getAllDevicesForProfiles(Seq<String> profileIds) {
		return collectDeviceAggregates(getDeviceAggregatesForProfiles(profileIds))
			.flatMap(aggregates -> getDevicesForAggregates(
					aggregates.map(DeviceAggregate::getId)));
	}


	public Future<Seq<Parameter>> getParameters(Seq<String> deviceIds) {
		return callService(parameterService, s -> s.getParameters(deviceIds.mkString(",")))
			.map(response -> List.ofAll(response.getParameters()));
	}

	public Future<Seq<Scenario>> getExperimentScenarios(String experimentId) {
		return callService(scenarioService, s -> s.getExperimentScenarios(experimentId))
			.map(response -> List.ofAll(response.getScenarios()));
	}

	public Future<Seq<Section>> getSections() {
		return callService(sectionService, s -> s.getSections())
			.map(response -> List.ofAll(response.getSections()));
	}

	public Future<Seq<Profile>> getProfiles(Seq<String> sectionIds) {
		return callService(profileService, s -> s.getProfilesForSection(sectionIds.mkString(",")))
			.map(response -> List.ofAll(response.getProfiles()));
	}

	public Future<Seq<Context>> getContexts(String contextType) {
		return callService(contextService, s -> s.getContexts(contextType))
			.map(response -> List.ofAll(response.getContexts()));
	}

	public Future<Seq<Timeline>> getTimelinesForParameterIds(String contextId,
			Seq<String> parameterIds) {
		return callService(timelineService, s -> s.getTimelines(contextId,
				parameterIds.mkString(",")))
			.map(response -> List.ofAll(response.getTimelines()));
	}

	public Future<Seq<Measurement>> getLastMeasurementsWith24HourMod(Seq<String> timelineIds,
			Date untilDate) {
		String until = ismopConverter.formatForDto(untilDate);
		String from = ismopConverter.formatForDto(new Date(untilDate.getTime() - 86_400_000L));

		return callService(measurementService, s -> s.getLastMeasurements(timelineIds.mkString(","),
				from, until))
			.map(response -> List.ofAll(response.getMeasurements()));
	}

	private <S extends DirectRestService, R> Future<R> callService(S service, Function<S, R> call) {
		Promise<R> promise = Promise.make();
		S s = REST.withCallback(new MethodCallback<R>() {

			@Override
			public void onFailure(Method method, Throwable exception) {
				promise.failure(exception);
			}

			@Override
			public void onSuccess(Method method, R response) {
				promise.success(response);
			}
		}).call(service);
		call.apply(s);

		return promise.future();
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregatesForProfile(String profileId) {
		return callService(deviceAggregateService, s -> s.getDeviceAggregates(profileId))
			.map(response -> List.ofAll(response.getDeviceAggregations()));
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregatesForProfiles(Seq<String> profileIds) {
		return callService(deviceAggregateService, s -> s.getDeviceAggregates(
				profileIds.mkString(",")))
			.map(response -> List.ofAll(response.getDeviceAggregations()));
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregates(Seq<String> ids) {
		return callService(deviceAggregateService, s -> s.getDeviceAggregates(ids.mkString(",")))
			.map(response -> List.ofAll(response.getDeviceAggregations()));
	}

	private Future<Seq<Device>> getDevicesForAggregates(Seq<String> aggregateIds) {
		return callService(deviceService, s -> s.getDevices(aggregateIds.mkString(",")))
			.map(response -> List.ofAll(response.getDevices()));
	}

	private Future<Seq<DeviceAggregate>> collectDeviceAggregates(
			Future<Seq<DeviceAggregate>> aggregatesFuture) {
		return aggregatesFuture.flatMap(aggregates -> {
			Seq<String> allChildrenIds = aggregates.flatMap(
					aggregate -> aggregate.getChildrenIds() == null
					? List.empty() : aggregate.getChildrenIds());

			if (allChildrenIds.isEmpty()) {
				return Future.successful(aggregates);
			} else {
				return collectDeviceAggregates(getDeviceAggregates(allChildrenIds))
						.map(childrenAggregates -> childrenAggregates.appendAll(aggregates));
			}
		});
	}
}
