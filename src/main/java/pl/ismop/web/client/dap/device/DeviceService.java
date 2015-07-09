package pl.ismop.web.client.dap.device;

import javax.websocket.server.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface DeviceService extends RestService {
	@GET
	@Path("devices?device_aggregation_id={deviceAggregationIdFilter}")
	void getDevices(@PathParam("deviceAggregationIdFilter") String deviceAggregationIdFilter, MethodCallback<DevicesResponse> callback);
}