package pl.ismop.web.client.dap.experiment;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;
import pl.ismop.web.client.dap.DapDispatcher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ExperimentService extends RestService {

    @GET
    @Path("experiments")
    void getExperiments(MethodCallback<ExperimentsResponse> callback);
}
