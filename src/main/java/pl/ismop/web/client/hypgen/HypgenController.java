package pl.ismop.web.client.hypgen;

import java.util.Date;
import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.inject.Inject;

public class HypgenController {
	private ExperimentService experimentService;

	public interface ErrorCallback {
		void onError(int code, String message);
	}
	
	public interface ExperimentCallback extends ErrorCallback {
		void processExperiment(Experiment experiment);
	}
	
	@Inject
	public HypgenController(ExperimentService experimentService) {
		this.experimentService = experimentService;
	}
	
	public void startExperiment(String name, List<String> profileIds, String days, final ExperimentCallback callback) {
		Experiment experiment = new Experiment();
		experiment.setName(name);
		experiment.setProfileIds(profileIds);
		
		Date now = new Date();
		Date before = new Date(now.getTime());
		CalendarUtil.addDaysToDate(before, -Integer.parseInt(days));
		experiment.setStartDate(before);
		experiment.setEndDate(now);
		
		ExperimentRequest request = new ExperimentRequest();
		request.setExperiment(experiment);
		experimentService.createExperiment(request, new MethodCallback<ExperimentResponse>() {
			@Override
			public void onSuccess(Method method, ExperimentResponse response) {
				callback.processExperiment(response.getExperiment());
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}
		});
	}
}