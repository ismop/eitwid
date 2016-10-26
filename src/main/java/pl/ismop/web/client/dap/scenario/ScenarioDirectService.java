package pl.ismop.web.client.dap.scenario;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface ScenarioDirectService extends DirectRestService {
    @GET
    @Path("scenarios?experiment_ids={experimentId}")
    ScenariosResponse getExperimentScenarios(@PathParam("experimentId") String experimentId);
}
