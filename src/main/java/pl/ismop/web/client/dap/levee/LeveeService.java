package pl.ismop.web.client.dap.levee;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class)
public interface LeveeService extends RestService {
	@GET
	@Path("levees")
	void getLevees(MethodCallback<LeveesResponse> callback);

	@PUT
	@Path("levees/{id}")
	void changeMode(@PathParam("id") String leveeId, ModeChangeRequest request, MethodCallback<LeveeResponse> methodCallback);

	@GET
	@Path("levees/{id}")
	void getLevee(@PathParam("id") String leveeId, MethodCallback<LeveeResponse> methodCallback);
}