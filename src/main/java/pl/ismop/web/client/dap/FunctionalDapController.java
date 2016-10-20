package pl.ismop.web.client.dap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javaslang.Function1;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.concurrent.Future;
import javaslang.concurrent.Promise;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.device.DeviceService;
import pl.ismop.web.client.dap.device.DevicesResponse;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateResponse;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateService;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.parameter.ParameterService;
import pl.ismop.web.client.dap.parameter.ParametersResponse;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.profile.ProfileService;
import pl.ismop.web.client.dap.profile.ProfilesResponse;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.dap.scenario.ScenarioService;
import pl.ismop.web.client.dap.scenario.ScenariosResponse;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.section.SectionService;
import pl.ismop.web.client.dap.section.SectionsResponse;

@Singleton
public class FunctionalDapController {

	private static class CallbackHandler<R, T> implements MethodCallback<T> {

		private Promise<R> promise;

		private Function1<T, R> mapping;

		public CallbackHandler(Promise<R> promise, Function1<T, R> mapping) {
			this.promise = promise;
			this.mapping = mapping;
		}

		@Override
		public void onFailure(Method method, Throwable exception) {
			promise.failure(exception);
		}

		@Override
		public void onSuccess(Method method, T response) {
			promise.success(mapping.apply(response));
		}
	}

	private DeviceService deviceService;

	private ProfileService profileService;

	private DeviceAggregateService deviceAggregateService;

	private ParameterService parameterService;

	private ScenarioService scenarioService;

	private SectionService sectionService;

	@Inject
	public FunctionalDapController(ProfileService profileService, DeviceService deviceService,
			DeviceAggregateService deviceAggregateService, ParameterService parameterService,
			ScenarioService scenarioService, SectionService sectionService) {
		this.profileService = profileService;
		this.deviceService = deviceService;
		this.deviceAggregateService = deviceAggregateService;
		this.parameterService = parameterService;
		this.scenarioService = scenarioService;
		this.sectionService = sectionService;
	}

	public Future<Seq<Device>> getAllDevicesForProfile(String profileId) {
		return collectDeviceAggregates(getDeviceAggregatesForProfile(profileId))
				.flatMap(aggregates -> getDevicesForAggregates(
						aggregates.map(DeviceAggregate::getId)));
	}

	public Future<Seq<Parameter>> getParameters(Seq<String> deviceIds) {
		Promise<Seq<Parameter>> result = Promise.make();
		parameterService.getParameters(deviceIds.mkString(","),
				new CallbackHandler<Seq<Parameter>, ParametersResponse>(
						result,
						response -> List.ofAll(response.getParameters())));

		return result.future();
	}

	public Future<Seq<Scenario>> getExperimentScenarios(String experimentId) {
		Promise<Seq<Scenario>> result = Promise.make();
		scenarioService.getExperimentScenarios(experimentId,
				new CallbackHandler<Seq<Scenario>, ScenariosResponse>(
						result,
						response -> List.ofAll(response.getScenarios())));

		return result.future();
	}

	public Future<Seq<Section>> getSections() {
		Promise<Seq<Section>> result = Promise.make();
		sectionService.getSections(new CallbackHandler<Seq<Section>, SectionsResponse>(
				result,
				response -> List.ofAll(response.getSections())));

		return result.future();
	}

	public Future<Seq<Profile>> getProfiles(Seq<String> sectionIds) {
		Promise<Seq<Profile>> result = Promise.make();
		profileService.getProfilesForSection(sectionIds.mkString(","),
				new CallbackHandler<Seq<Profile>, ProfilesResponse>(
						result,
						response -> List.ofAll(response.getProfiles())));

		return result.future();
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregatesForProfile(String profileId) {
		Promise<Seq<DeviceAggregate>> result = Promise.make();
		deviceAggregateService.getDeviceAggregates(profileId,
				new CallbackHandler<Seq<DeviceAggregate>, DeviceAggregateResponse>(
						result,
						response -> List.ofAll(response.getDeviceAggregations())));

		return result.future();
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregates(Seq<String> ids) {
		Promise<Seq<DeviceAggregate>> result = Promise.make();
		deviceAggregateService.getDeviceAggregatesForIds(ids.mkString(","),
				new CallbackHandler<Seq<DeviceAggregate>, DeviceAggregateResponse>(
						result,
						response -> List.ofAll(response.getDeviceAggregations())));

		return result.future();
	}

	private Future<Seq<Device>> getDevicesForAggregates(Seq<String> aggregateIds) {
		Promise<Seq<Device>> result = Promise.make();
		deviceService.getDevices(aggregateIds.mkString(","),
				new CallbackHandler<Seq<Device>, DevicesResponse>(
						result,
						response -> List.ofAll(response.getDevices())));

		return result.future();
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
