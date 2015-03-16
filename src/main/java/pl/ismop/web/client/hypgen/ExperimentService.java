package pl.ismop.web.client.hypgen;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

@Options(dispatcher = HypgenDispatcher.class, serviceRootKey = "hypgen")
public interface ExperimentService extends RestService {
	@POST
	@Path("threat_assessments")
	void createExperiment(ExperimentRequest request, MethodCallback<ExperimentResponse> callback);
}