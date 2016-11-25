package pl.ismop.web.client.dap.context;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ContextDirectService extends DirectRestService {
	@GET
	@Path("contexts?context_type={contextType}")
	ContextsResponse getContexts(@PathParam("contextType") String contextType);

	@GET
	@Path("contexts?id={contextId}")
	ContextsResponse getContextsById(@PathParam("contextId") String contextId);
}
