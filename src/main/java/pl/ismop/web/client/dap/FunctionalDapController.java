package pl.ismop.web.client.dap;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import javaslang.Function1;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.concurrent.Future;
import javaslang.concurrent.Promise;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.device.DeviceDirectService;
import pl.ismop.web.client.dap.device.DevicesResponse;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateDirectService;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateResponse;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.parameter.ParameterDirectService;
import pl.ismop.web.client.dap.parameter.ParametersResponse;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.profile.ProfileDirectService;
import pl.ismop.web.client.dap.profile.ProfilesResponse;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.dap.scenario.ScenarioDirectService;
import pl.ismop.web.client.dap.scenario.ScenariosResponse;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.section.SectionDirectService;
import pl.ismop.web.client.dap.section.SectionsResponse;

@Singleton
public class FunctionalDapController {

	private static class ServiceCall<S extends DirectRestService, R> {

		private Consumer<S> serviceCall;

		private S service;

		public ServiceCall(S service, Consumer<S> serviceCall) {
			this.service = service;
			this.serviceCall = serviceCall;
		}

		public <E> Future<Seq<E>> andThen(Function1<R, Seq<E>> transformResult) {
			Promise<Seq<E>> promise = Promise.make();
			S s = REST.withCallback(new MethodCallback<R>() {

				@Override
				public void onFailure(Method method, Throwable exception) {
					promise.failure(exception);
				}

				@Override
				public void onSuccess(Method method, R response) {
					promise.success(transformResult.apply(response));
				}
			}).call(service);
			serviceCall.accept(s);

			return promise.future();
		}
	}

	private DeviceDirectService deviceService;

	private ProfileDirectService profileService;

	private DeviceAggregateDirectService deviceAggregateService;

	private ParameterDirectService parameterService;

	private ScenarioDirectService scenarioService;

	private SectionDirectService sectionService;

	@Inject
	public FunctionalDapController(ProfileDirectService profileService,
			DeviceDirectService deviceService, DeviceAggregateDirectService deviceAggregateService,
			ParameterDirectService parameterService, ScenarioDirectService scenarioService,
			SectionDirectService sectionService) {
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

	public Future<Seq<Device>> getAllDevicesForProfiles(Seq<String> profileIds) {
		return collectDeviceAggregates(getDeviceAggregatesForProfiles(profileIds))
				.flatMap(aggregates -> getDevicesForAggregates(
						aggregates.map(DeviceAggregate::getId)));
	}

	public Future<Seq<Parameter>> getParameters(Seq<String> deviceIds) {
		return new ServiceCall<ParameterDirectService, ParametersResponse>(
					parameterService, service -> service.getParameters(deviceIds.mkString(",")))
				.andThen(response -> List.ofAll(response.getParameters()));
	}

	public Future<Seq<Scenario>> getExperimentScenarios(String experimentId) {
		return new ServiceCall<ScenarioDirectService, ScenariosResponse>(
					scenarioService, service -> service.getExperimentScenarios(experimentId))
				.andThen(response -> List.ofAll(response.getScenarios()));
	}

	public Future<Seq<Section>> getSections() {
		return new ServiceCall<SectionDirectService, SectionsResponse>(
					sectionService, service -> service.getSections())
				.andThen(response -> List.ofAll(response.getSections()));
	}

	public Future<Seq<Profile>> getProfiles(Seq<String> sectionIds) {
		return new ServiceCall<ProfileDirectService, ProfilesResponse>(
					profileService,
					service -> service.getProfilesForSection(sectionIds.mkString(",")))
				.andThen(response -> List.ofAll(response.getProfiles()));
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregatesForProfile(String profileId) {
		return new ServiceCall<DeviceAggregateDirectService, DeviceAggregateResponse>(
					deviceAggregateService, service -> service.getDeviceAggregates(profileId))
				.andThen(response -> List.ofAll(response.getDeviceAggregations()));
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregatesForProfiles(Seq<String> profileIds) {
		return new ServiceCall<DeviceAggregateDirectService, DeviceAggregateResponse>(
				deviceAggregateService, service -> service.getDeviceAggregates(
						profileIds.mkString(",")))
			.andThen(response -> List.ofAll(response.getDeviceAggregations()));
	}

	private Future<Seq<DeviceAggregate>> getDeviceAggregates(Seq<String> ids) {
		return new ServiceCall<DeviceAggregateDirectService, DeviceAggregateResponse>(
				deviceAggregateService, service -> service.getDeviceAggregates(ids.mkString(",")))
			.andThen(response -> List.ofAll(response.getDeviceAggregations()));
	}

	private Future<Seq<Device>> getDevicesForAggregates(Seq<String> aggregateIds) {
		return new ServiceCall<DeviceDirectService, DevicesResponse>(
					deviceService, service -> service.getDevices(aggregateIds.mkString(",")))
				.andThen(response -> List.ofAll(response.getDevices()));
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
