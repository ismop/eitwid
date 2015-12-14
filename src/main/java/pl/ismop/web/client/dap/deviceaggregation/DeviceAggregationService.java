package pl.ismop.web.client.dap.deviceaggregation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface DeviceAggregationService extends RestService {
	@GET
	@Path("device_aggregations?profile_id={profileIdFilter}")
	void getDeviceAggregations(@PathParam("profileIdFilter") String profileIdFilter, MethodCallback<DeviceAggregationsResponse> callback);

	@GET
	@Path("device_aggregations?id={idFilter}")
	void getDeviceAggregationsForIds(@PathParam("idFilter") String idFilter, MethodCallback<DeviceAggregationsResponse> methodCallback);

	@GET
	@Path("device_aggregations?device_aggregation_type={type}")
	void getDeviceAggregationsForType(@PathParam("type") String type, MethodCallback<DeviceAggregationsResponse> methodCallback);

	@GET
	@Path("device_aggregations?device_aggregation_type={type}&levee_id={levee_id}")
	void getDeviceAggregationsForType(@PathParam("type") String type, @PathParam("levee_id") String leveeId,
									  MethodCallback<DeviceAggregationsResponse> methodCallback);
	
	@GET
	@Path("device_aggregations?section_id={sectionIdFilter}")
	void getDeviceAggregationsForSectionIds(@PathParam("sectionIdFilter") String sectionIdFilter, MethodCallback<DeviceAggregationsResponse> methodCallback);
}