package pl.ismop.web.client.dap.section;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * This synchronous version is used by the server-side communication with DAP.
 */
public interface SectionServiceSync {
	@GET
	@Path("sections")
	@Produces("application/json")
	SectionsResponse getSections();
	
	@GET
	@Path("sections?selection={selection}")
	@Produces("application/json")
	SectionsResponse getProfiles(@PathParam("selection") String selection);
}