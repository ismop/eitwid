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
}