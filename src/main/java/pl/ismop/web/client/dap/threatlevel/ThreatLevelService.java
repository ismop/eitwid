package pl.ismop.web.client.dap.threatlevel;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ThreatLevelService extends RestService {
	@GET
	@Path("threat_levels?limit={limit}&from={from}&to={to}&status={status}")
	void getThreatLevels(@PathParam("limit") int limit, @PathParam("from") Date from, @PathParam("to") Date to,
			String status, MethodCallback<ThreatLevelResponse> callback);
}
