package pl.ismop.web.client.dap.result;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class)
public interface ResultService extends RestService {
	@GET
	@Path("https://dap.moc.ismop.edu.pl/api/v1/results?experiment_id={experimentId}")
	void getResults(@PathParam("experimentId") String experimentId, MethodCallback<ResultsResponse> callback);
}