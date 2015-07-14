package pl.ismop.web.client.dap.timeline;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface TimelineService extends RestService {
	@GET
	@Path("timelines?context_id={contextId}&paramter_id={parameterId}")
	void getTimelines(@PathParam("contextId") String contextId, @PathParam("parameterId") String parameterId, MethodCallback<TimelinesResponse> callback);
}