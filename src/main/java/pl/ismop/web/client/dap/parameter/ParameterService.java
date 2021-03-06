package pl.ismop.web.client.dap.parameter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ParameterService extends RestService {
	@GET
	@Path("parameters?device_id={deviceId}")
	void getParameters(@PathParam("deviceId") String deviceId, MethodCallback<ParametersResponse> callback);

	@GET
	@Path("parameters?id={id}")
	void getParametersById(@PathParam("id") String id, MethodCallback<ParametersResponse> callback);

	@GET
	@Path("parameters?levee_id={leveeId}")
	void getLeveeParameters(@PathParam("leveeId") Integer leveeId, MethodCallback<ParametersResponse> callback);
}
