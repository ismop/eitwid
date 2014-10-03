package pl.ismop.web.client.internal;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.model.UserExperiments;

@Options(dispatcher = InternalExperimentDispatcher.class)
public interface InternalExperimentService extends RestService {
	@GET
	@Path("../experiments")
	void getExperiments(MethodCallback<UserExperiments> callback);
	
	@PUT
	@Path("../experiments")
	void addExperiment(Experiment experiment, MethodCallback<Void> callback);
}