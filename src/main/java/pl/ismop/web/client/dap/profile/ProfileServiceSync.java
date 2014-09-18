package pl.ismop.web.client.dap.profile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * This synchronous version is used by the server-side communication with DAP.
 */
public interface ProfileServiceSync {
	@GET
	@Path("profiles")
	@Produces("application/json")
	ProfilesResponse getProfiles();
	
	@GET
	@Path("profiles?selection={selection}")
	@Produces("application/json")
	ProfilesResponse getProfiles(@PathParam("selection") String selection);
}