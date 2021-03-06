package pl.ismop.web.client.internal;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.model.UserExperiments;

public class InternalExperimentController {
	private InternalExperimentService experimentService;
	private ExperimentPlanService experimentPlanService;

	public interface ErrorCallback {
		void onError(int code, String message);
	}
	
	public interface InternalExperimentCallback extends ErrorCallback {
		void experimentAdded();
	}
	
	public interface UserExperimentsCallback extends ErrorCallback {
		void processUserExperiments(List<String> experimentIds);
	}
	
	public interface ExperimentPlansCallback extends ErrorCallback {
		void processExperimentPlans(List<ExperimentPlanBean> experimentPlans);
	}
	
	@Inject
	public InternalExperimentController(InternalExperimentService experimentService, ExperimentPlanService experimentPlanService) {
		this.experimentService = experimentService;
		this.experimentPlanService = experimentPlanService;
	}
	
	public void addExperiment(String experimentId, final InternalExperimentCallback callback) {
		Experiment experiment = new Experiment();
		experiment.setId(experimentId);
		experimentService.addExperiment(experiment, new MethodCallback<Void>() {
			@Override
			public void onSuccess(Method method, Void response) {
				callback.experimentAdded();
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}
		});
	}
	
	public void getExperiments(final UserExperimentsCallback callback) {
		experimentService.getExperiments(new MethodCallback<UserExperiments>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, UserExperiments response) {
				callback.processUserExperiments(response.getExperimentIds());
			}
		});
	}

	public void getExperimentPlans(final ExperimentPlansCallback callback) {
		experimentPlanService.getExperimentPlans(new MethodCallback<ExperimentPlanResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage() + " " + exception.getCause().getMessage());
			}

			@Override
			public void onSuccess(Method method, ExperimentPlanResponse response) {
				if(response.getEmbedded() != null && response.getEmbedded().getExperimentPlans() != null) {
					callback.processExperimentPlans(response.getEmbedded().getExperimentPlans());
				} else {
					callback.processExperimentPlans(new ArrayList<ExperimentPlanBean>());
				}
			}
		});
	}

	public void addExperimentPlan(ExperimentPlanBean experimentPlan) {
		experimentPlanService.addExperimentPlan(experimentPlan, new MethodCallback<Void>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, Void response) {
				Window.alert("Done");
			}
		});
	}
	
	public void deleteExperimentPlan(final String experimentPlanId) {
		experimentPlanService.deleteExperimentPlan(experimentPlanId, new MethodCallback<Void>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, Void response) {
				Window.alert("Experiment with id " + experimentPlanId + " successfully deleted");
			}
		});
	}
}