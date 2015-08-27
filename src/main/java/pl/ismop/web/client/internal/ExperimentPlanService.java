package pl.ismop.web.client.internal;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

@Options(dispatcher = InternalExperimentDispatcher.class)
public interface ExperimentPlanService extends RestService {
	@GET
	@Path("../experimentPlans")
	void getExperimentPlans(MethodCallback<ExperimentPlanResponse> callback);
	
	@POST
	@Path("../experimentPlans")
	void addExperimentPlan(ExperimentPlanBean experiment, MethodCallback<Void> callback);

	@DELETE
	@Path("../experimentPlans/{experimentPlanId}")
	void deleteExperimentPlan(@PathParam("experimentPlanId") String experimentPlanId, MethodCallback<Void> methodCallback);
}