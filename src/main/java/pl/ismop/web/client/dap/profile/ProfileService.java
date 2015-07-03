package pl.ismop.web.client.dap.profile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ProfileService extends RestService {
	@GET
	@Path("profiles?sectionId={sectionId}")
	void getProfilesForSection(@PathParam("sectionId") String sectionId, MethodCallback<ProfilesResponse> callback);
	
	@GET
	@Path("profiles?selection={selection}")
	void getProfiles(@PathParam("selection") String selection, MethodCallback<ProfilesResponse> callback);
}