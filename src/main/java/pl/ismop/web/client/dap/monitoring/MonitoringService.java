package pl.ismop.web.client.dap.monitoring;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface MonitoringService extends RestService {
	@GET
	@Path("monitoring")
	void getMonitoringInfo(MethodCallback<MonitoringResponse> callback);
}