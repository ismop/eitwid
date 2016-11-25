package pl.ismop.web.client.dap.section;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface SectionDirectService extends DirectRestService {
	@GET
	@Path("sections")
	SectionsResponse getSections();

	@GET
	@Path("sections?levee_id={leveeId}")
	SectionsResponse getSectionsForLevee(@PathParam("leveeId") String leveeId);

	@GET
	@Path("sections?selection={selection}")
	SectionsResponse getSections(@PathParam("selection") String selection);

	@GET
	@Path("sections?id={id}")
	SectionsResponse getSectionsById(@PathParam("id") String id);
}
