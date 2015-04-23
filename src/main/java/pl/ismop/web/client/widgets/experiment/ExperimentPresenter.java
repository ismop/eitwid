package pl.ismop.web.client.widgets.experiment;

import static java.util.Arrays.asList;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ExperimentsCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.experiment.IExperimentView.IExperimentPresenter;
import pl.ismop.web.client.widgets.experimentitem.ExperimentItemPresenter;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = ExperimentView.class)
public class ExperimentPresenter extends BasePresenter<IExperimentView, MainEventBus> implements IExperimentPresenter {
	private DapController dapController;
	private ExperimentItemPresenter presenter;

	@Inject
	public ExperimentPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowExperiment() {
		eventBus.setTitleAndShow(view.getMainTitle(), view, false);
		dapController.getExperiments(asList("12"), new ExperimentsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processExperiments(List<Experiment> experiments) {
				if(presenter != null) {
					eventBus.removeHandler(presenter);
				}
				
				presenter = eventBus.addHandler(ExperimentItemPresenter.class);
				presenter.setExperiment(experiments.get(0));
				view.addAnalysis(presenter.getView());
			}
		});
	}
	
	private native void push(double value, String timestamp, JavaScriptObject values) /*-{
		values.push({value: value, timestamp: timestamp});
	}-*/;
	
	private native JavaScriptObject showDygraphChart(String values, String yLabel, String title) /*-{
		return new $wnd.Dygraph($doc.getElementById('singlePlot'), values, {
			showRangeSelector: true,
			ylabel: yLabel,
			labelsDivStyles: {
				textAlign: 'right'
			},
			axisLabelWidth: 100,
			title: title,
			digitsAfterDecimal: 1,
			delimiter: '|'
		});
	}-*/;
	
	private String getDygraphValues(List<Measurement> measurements, String yLabel) {
		StringBuilder builder = new StringBuilder();
		builder.append("aa|").append(yLabel).append("\n");
		
		for(Measurement measurement : measurements) {
			DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
			Date date = format.parse(measurement.getTimestamp());
			date = new Date(date.getTime() - 7200000);
			builder.append(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(date))
					.append("|")
					.append(measurement.getValue())
					.append("\n");
		}
		
		return builder.toString();
	}

	@Override
	public void showPlot() {
		dapController.getMeasurements("110", new MeasurementsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}

			@Override
			public void processMeasurements(List<Measurement> measurements) {
				if(measurements.size() == 0) {
				} else {
					JavaScriptObject values = JavaScriptObject.createArray();
					double min = Double.MAX_VALUE;
					double max = Double.MIN_VALUE;
					
					for(Measurement measurement : measurements) {
						push(measurement.getValue(), measurement.getTimestamp(), values);
						
						if(measurement.getValue() < min) {
							min = measurement.getValue();
						}
						
						if(measurement.getValue() > max) {
							max = measurement.getValue();
						}
					}
					
					double diff = max- min;
					min = min - 0.1 * diff;
					max = max + 0.1 * diff;
					
					String unitLabel = "Temperature";
					String unit = "C";
					showDygraphChart(getDygraphValues(measurements, unitLabel), unitLabel + ", " + unit,
							unitLabel + " (00034)");
				}
			}});
	}
}