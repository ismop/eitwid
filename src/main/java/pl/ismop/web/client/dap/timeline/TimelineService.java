package pl.ismop.web.client.dap.timeline;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;
import pl.ismop.web.client.dap.DapDispatcher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface TimelineService extends RestService {
	@GET
	@Path("timelines?context_id={contextId}&parameter_id={parameterId}")
	void getTimelines(@PathParam("contextId") String contextId, @PathParam("parameterId") String parameterId, MethodCallback<TimelinesResponse> callback);

	@GET
	@Path("timelines?experiment_id={experimentId}")
	void getExperimentTimelines(@PathParam("experimentId") String experimentId,
								 MethodCallback<TimelinesResponse> callback);
}