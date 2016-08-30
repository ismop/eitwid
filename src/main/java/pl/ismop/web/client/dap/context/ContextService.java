package pl.ismop.web.client.dap.context;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ContextService extends RestService {
	@GET
	@Path("contexts?context_type={contextType}")
	void getContexts(@PathParam("contextType") String contextType, MethodCallback<ContextsResponse> callback);

	@GET
	@Path("contexts?id={contextId}")
	void getContextsById(@PathParam("contextId") String contextId, MethodCallback<ContextsResponse> callback);
}
