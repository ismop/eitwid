package pl.ismop.web.client.dap.parameter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ParameterDirectService extends DirectRestService {

	@GET
	@Path("parameters?device_id={deviceId}")
	ParametersResponse getParameters(@PathParam("deviceId") String deviceId);
}
