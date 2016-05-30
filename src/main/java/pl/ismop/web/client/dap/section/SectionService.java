package pl.ismop.web.client.dap.section;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface SectionService extends RestService {
	@GET
	@Path("sections")
	void getSections(MethodCallback<SectionsResponse> callback);
	
	@GET
	@Path("sections?levee_id={leveeId}")
	void getSectionsForLevee(@PathParam("leveeId") String leveeId, MethodCallback<SectionsResponse> callback);
	
	@GET
	@Path("sections?selection={selection}")
	void getSections(@PathParam("selection") String selection, MethodCallback<SectionsResponse> callback);

	@GET
	@Path("sections?id={id}")
	void getSectionsById(@PathParam("id") String id, MethodCallback<SectionsResponse> callback);
}