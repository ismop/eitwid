package pl.ismop.web.client.dap.scenario;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;
import pl.ismop.web.client.dap.DapDispatcher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ScenarioService extends RestService {
    @GET
    @Path("scenarios?experiment_ids={experimentId}")
    void getExperimentScenarios(@PathParam("experimentId") String experimentId,
                                MethodCallback<ScenariosResponse> callback);
}
