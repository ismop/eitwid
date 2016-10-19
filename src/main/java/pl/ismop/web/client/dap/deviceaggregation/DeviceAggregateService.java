package pl.ismop.web.client.dap.deviceaggregation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface DeviceAggregateService extends RestService {
	@GET
	@Path("device_aggregations?profile_id={profileIdFilter}")
	void getDeviceAggregates(@PathParam("profileIdFilter") String profileIdFilter,
			MethodCallback<DeviceAggregateResponse> callback);

	@GET
	@Path("device_aggregations?id={idFilter}")
	void getDeviceAggregatesForIds(@PathParam("idFilter") String idFilter,
			MethodCallback<DeviceAggregateResponse> methodCallback);

	@GET
	@Path("device_aggregations?device_aggregation_type={type}")
	void getDeviceAggregatesForType(@PathParam("type") String type,
			MethodCallback<DeviceAggregateResponse> methodCallback);

	@GET
	@Path("device_aggregations?device_aggregation_type={type}&levee_id={levee_id}")
	void getDeviceAggregatesForType(@PathParam("type") String type,
			@PathParam("levee_id") String leveeId,
			MethodCallback<DeviceAggregateResponse> methodCallback);

	@GET
	@Path("device_aggregations?section_id={sectionIdFilter}")
	void getDeviceAggregatesForSectionIds(@PathParam("sectionIdFilter") String sectionIdFilter,
			MethodCallback<DeviceAggregateResponse> methodCallback);
}
