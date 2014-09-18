package pl.ismop.web.client.dap.levee;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * This synchronous version is used by the server-side communication with DAP.
 */
public interface LeveeServiceSync {
	@GET
	@Path("levees")
	@Produces("application/json")
	LeveesResponse getLevees();

	@PUT
	@Path("levees/{id}")
	@Produces("application/json")
	@Consumes("application/json")
	LeveeResponse changeMode(@PathParam("id") String leveeId, ModeChangeRequest request);

	@GET
	@Path("levees/{id}")
	@Produces("application/json")
	LeveeResponse getLevee(@PathParam("id") String leveeId);
}