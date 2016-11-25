package pl.ismop.web.client.dap.timeline;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface TimelineDirectService extends DirectRestService {
	@GET
	@Path("timelines?context_id={contextId}&parameter_id={parameterId}")
	TimelinesResponse getTimelines(@PathParam("contextId") String contextId,
			@PathParam("parameterId") String parameterId);

	@GET
	@Path("timelines?experiment_id={experimentId}")
	TimelinesResponse getExperimentTimelines(@PathParam("experimentId") String experimentId);

	@GET
	@Path("timelines?parameter_id={parameterId}")
	TimelinesResponse getParameterTimelines(@PathParam("parameterId") String parameterId);
}
