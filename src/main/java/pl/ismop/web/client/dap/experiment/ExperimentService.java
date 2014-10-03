package pl.ismop.web.client.dap.experiment;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class)
public interface ExperimentService extends RestService {
	@GET
	@Path("https://dap.moc.ismop.edu.pl/api/v1/experiments?id={ids}")
	void getExperiments(@PathParam("ids") String ids, MethodCallback<ExperimentsResponse> callback);
}