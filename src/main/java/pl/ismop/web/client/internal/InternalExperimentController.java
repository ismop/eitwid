package pl.ismop.web.client.internal;

import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.model.UserExperiments;

import com.google.inject.Inject;

public class InternalExperimentController {
	private InternalExperimentService experimentService;

	public interface ErrorCallback {
		void onError(int code, String message);
	}
	
	public interface InternalExperimentCallback extends ErrorCallback {
		void experimentAdded();
	}
	
	public interface UserExperimentsCallback extends ErrorCallback {
		void processUserExperiments(List<String> experimentIds);
	}
	
	@Inject
	public InternalExperimentController(InternalExperimentService experimentService) {
		this.experimentService = experimentService;
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
}