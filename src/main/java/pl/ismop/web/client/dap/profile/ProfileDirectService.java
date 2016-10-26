package pl.ismop.web.client.dap.profile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ProfileDirectService extends DirectRestService {
	@GET
	@Path("profiles?section_id={sectionIdFilter}")
	ProfilesResponse getProfilesForSection(@PathParam("sectionIdFilter") String sectionIdFilter);

	@GET
	@Path("profiles?selection={selection}")
	ProfilesResponse getProfiles(@PathParam("selection") String selection);
}
