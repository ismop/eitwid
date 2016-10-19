package pl.ismop.web.client.dap;

import java.util.function.BiFunction;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javaslang.Function1;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.concurrent.Future;
import javaslang.concurrent.Promise;
import javaslang.control.Try;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.device.DeviceService;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateResponse;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregateService;
import pl.ismop.web.client.dap.profile.ProfileService;

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

	@Inject
	public FunctionalDapController(ProfileService profileService, DeviceService deviceService,
			DeviceAggregateService deviceAggregateService) {
		this.profileService = profileService;
		this.deviceService = deviceService;
		this.deviceAggregateService = deviceAggregateService;
	}

	public Future<Try<Seq<Device>>> getAllDevicesForProfile(String profileId) {
		Promise<Try<Seq<Device>>> result = Promise.make();
		collectDeviceAggregateIds(getDeviceAggregatesForProfile(profileId));

		return result.future();
	}

	public Future<Try<Seq<DeviceAggregate>>> getDeviceAggregatesForProfile(String profileId) {
		Promise<Try<Seq<DeviceAggregate>>> result = Promise.make();
		deviceAggregateService.getDeviceAggregates(profileId,
				new CallbackHandler<Try<Seq<DeviceAggregate>>, DeviceAggregateResponse>(
						result,
						response -> Try.success(List.ofAll(response.getDeviceAggregations()))));

		return result.future();
	}

	public Future<Try<Seq<DeviceAggregate>>> getDeviceAggregates(Seq<String> ids) {
		Promise<Try<Seq<DeviceAggregate>>> result = Promise.make();
		deviceAggregateService.getDeviceAggregatesForIds(ids.mkString(","),
				new CallbackHandler<Try<Seq<DeviceAggregate>>, DeviceAggregateResponse>(
						result,
						response -> Try.success(List.ofAll(response.getDeviceAggregations()))));

		return result.future();
	}

	private Future<Try<Seq<DeviceAggregate>>> collectDeviceAggregateIds(
			Future<Try<Seq<DeviceAggregate>>> inputDeviceAggregatesTry) {
		return inputDeviceAggregatesTry.flatMap(deviceAggregatesTry -> {
			if (deviceAggregatesTry.isSuccess()) {
				Seq<Future<Try<Seq<DeviceAggregate>>>> futures = deviceAggregatesTry.get()
					.filter(deviceAggregate -> !deviceAggregate.getChildrenIds().isEmpty())
					.map(aggregateWithChildren -> getDeviceAggregates(
							List.ofAll(aggregateWithChildren.getChildrenIds())));
				Future<Try<Seq<DeviceAggregate>>> zero = Future.successful(
						Try.success(deviceAggregatesTry.get()));
				BiFunction<
						Future<Seq<DeviceAggregate>>,
						Future<Seq<DeviceAggregate>>,
						Future<Seq<DeviceAggregate>>> f =
					(result, future) -> result.flatMap(seq -> future.map(seq::append));


			} else {
				return Future.failed(deviceAggregatesTry.getCause());
			}
		});
	}
}
