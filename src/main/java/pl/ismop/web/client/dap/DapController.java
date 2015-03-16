package pl.ismop.web.client.dap;

import java.util.Date;
import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeResponse;
import pl.ismop.web.client.dap.levee.LeveeService;
import pl.ismop.web.client.dap.levee.LeveesResponse;
import pl.ismop.web.client.dap.levee.ModeChange;
import pl.ismop.web.client.dap.levee.ModeChangeRequest;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.measurement.MeasurementService;
import pl.ismop.web.client.dap.measurement.MeasurementsResponse;
import pl.ismop.web.client.dap.result.Result;
import pl.ismop.web.client.dap.result.ResultService;
import pl.ismop.web.client.dap.result.ResultsResponse;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.section.SectionService;
import pl.ismop.web.client.dap.section.SectionsResponse;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.dap.sensor.SensorResponse;
import pl.ismop.web.client.dap.sensor.SensorService;
import pl.ismop.web.client.dap.threatassessment.ThreatAssessmentService;
import pl.ismop.web.client.dap.threatassessment.ThreatAssessmentResponse;
import pl.ismop.web.client.hypgen.Experiment;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DapController {
	private LeveeService leveeService;
	private SensorService sensorService;
	private MeasurementService measurementService;
	private SectionService sectionService;
	private ThreatAssessmentService experimentService;
	private ResultService resultService;
	
	public interface ErrorCallback {
		void onError(int code, String message);
	}
	
	public interface LeveesCallback extends ErrorCallback {
		void processLevees(List<Levee> levees);
	}
	
	public interface LeveeCallback extends ErrorCallback {
		void processLevee(Levee levee);
	}
	
	public interface SensorCallback extends ErrorCallback {
		void processSensor(Sensor sensor);
	}
	
	public interface MeasurementsCallback extends ErrorCallback {
		void processMeasurements(List<Measurement> measurements);
	}
	
	public interface SectionsCallback extends ErrorCallback {
		void processSections(List<Section> profiles);
	}
	
	public interface ExperimentsCallback extends ErrorCallback {
		void processExperiments(List<Experiment> experiments);
	}
	
	public interface ResultsCallback extends ErrorCallback {
		void processResults(List<Result> results);
	}

	@Inject
	public DapController(LeveeService leveeService, SensorService sensorService,
			MeasurementService measurementService, SectionService sectionService,
			ThreatAssessmentService experimentService, ResultService resultService) {
		this.leveeService = leveeService;
		this.sensorService = sensorService;
		this.measurementService = measurementService;
		this.sectionService = sectionService;
		this.experimentService = experimentService;
		this.resultService = resultService;
	}
	
	public void getLevees(final LeveesCallback callback) {	
		leveeService.getLevees(new MethodCallback<LeveesResponse>() {
			@Override
			public void onSuccess(Method method, LeveesResponse response) {
				callback.processLevees(response.getLevees());
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}
		});
	}

	public void changeLeveeMode(String leveeId, String mode, final LeveeCallback callback) {
		ModeChange modeChange = new ModeChange();
		modeChange.setMode(mode);
		
		ModeChangeRequest request = new ModeChangeRequest();
		request.setModeChange(modeChange);
		leveeService.changeMode(leveeId, request, new MethodCallback<LeveeResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, LeveeResponse response) {
				callback.processLevee(response.getLevee());
			}});
	}

	public void getLevee(String leveeId, final LeveeCallback callback) {
		leveeService.getLevee(leveeId, new MethodCallback<LeveeResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, LeveeResponse response) {
				callback.processLevee(response.getLevee());
			}});
	}

	public void getSensor(String sensorId, final SensorCallback callback) {
		sensorService.getSensor(sensorId, new MethodCallback<SensorResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SensorResponse response) {
				callback.processSensor(response.getSensor());
			}
		});
	}

	public void getMeasurements(String sensorId, final MeasurementsCallback callback) {
		String until = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(new Date());
		String from = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(new Date(new Date().getTime() - 2678400000L));//fetching one month old data
		measurementService.getMeasurements(sensorId, from, until, new MethodCallback<MeasurementsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, MeasurementsResponse response) {
				callback.processMeasurements(response.getMeasurements());
			}
		});
	}
	
	public void getSections(float top, float left, float bottom, float right, final SectionsCallback callback) {
		sectionService.getSections(createSelectionQuery(top, left, bottom, right), new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				callback.processSections(response.getSections());
			}
		});
	}
	
	public void getExperiments(List<String> experimentIds, final ExperimentsCallback callback) {
		experimentService.getExperiments(merge(experimentIds, ","), new MethodCallback<ThreatAssessmentResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ThreatAssessmentResponse response) {
				callback.processExperiments(response.getExperiments());
			}
		});
	}
	
	public void getResults(String experimentId, final ResultsCallback callback) {
		resultService.getResults(experimentId, new MethodCallback<ResultsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, ResultsResponse response) {
				callback.processResults(response.getResults());
			}
		});
	}
	
	public void getSections(final SectionsCallback sectionsCallback) {
		sectionService.getProfiles(new MethodCallback<SectionsResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, SectionsResponse response) {
				sectionsCallback.processSections(response.getSections());
			}
		});
	}

	private String merge(List<String> chunks, String delimeter) {
		StringBuilder result = new StringBuilder();
		
		for(String chunk : chunks) {
			result.append(chunk).append(delimeter);
		}
		
		if(result.length() > 0) {
			result.delete(result.length() - delimeter.length(), result.length());
		}
		
		return result.toString();
	}

	private String createSelectionQuery(double top, double left, double bottom, double right) {
		StringBuilder builder = new StringBuilder();
		builder.append("POLYGON ((")
				.append(left).append(" ").append(top).append(", ")
				.append(right).append(" ").append(top).append(", ")
				.append(right).append(" ").append(bottom).append(", ")
				.append(left).append(" ").append(bottom).append(", ")
				.append(left).append(" ").append(top)
				.append("))");
		
		return builder.toString();
	}
}