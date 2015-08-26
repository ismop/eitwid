package pl.ismop.web.client.internal;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

@Options(dispatcher = InternalExperimentDispatcher.class)
public interface ExperimentPlanService extends RestService {
	@GET
	@Path("../experimentPlans")
	void getExperimentPlans(MethodCallback<ExperimentPlanList> callback);
	
	@POST
	@Path("../experimentPlans")
	void addExperimentPlan(ExperimentPlanBean experiment, MethodCallback<Void> callback);
}