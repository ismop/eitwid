package pl.ismop.web.client.widgets.plot;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ContextsCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.ParametersCallback;
import pl.ismop.web.client.dap.DapController.TimelineCallback;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.plot.IPlotView.IPlotPresenter;

@Presenter(view = PlotView.class, multiple = true)
public class PlotPresenter extends BasePresenter<IPlotView, MainEventBus> implements IPlotPresenter {
	private DapController dapController;
	private JavaScriptObject graph;

	@Inject
	public PlotPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void drawMeasurements(List<String> deviceIds) {
		view.showMessageLabel(false);
		//for now let's just plot single time series
		if(deviceIds.size() > 0) {
			final String deviceId = deviceIds.get(0);
			dapController.getParameters(deviceId, new ParametersCallback() {
				@Override
				public void onError(int code, String message) {
					Window.alert(message);
				}
				
				@Override
				public void processParamters(List<Parameter> parameters) {
					if(parameters.size() > 0) {
						//there should be exactly one parameter for each device
						final Parameter parameter = parameters.get(0);
						dapController.getContext("tests", new ContextsCallback() {
							@Override
							public void onError(int code, String message) {
								Window.alert(message);
							}
							
							@Override
							public void processContexts(List<Context> contexts) {
								if(contexts.size() > 0) {
									//there should be only one context
									Context context = contexts.get(0);
									dapController.getTimeline(context.getId(), parameter.getId(), new TimelineCallback() {
										@Override
										public void onError(int code, String message) {
											Window.alert(message);
										}
										
										@Override
										public void processTimelines(List<Timeline> timelines) {
											if(timelines.size() > 0) {
												//there should be only one timeline
												Timeline timeline = timelines.get(0);
												dapController.getMeasurements(timeline.getId(), new MeasurementsCallback() {
													@Override
													public void onError(int code, String message) {
														Window.alert(message);
													}
													
													@Override
													public void processMeasurements(List<Measurement> measurements) {
														if(measurements.size() == 0) {
															view.showMessageLabel(true);
															view.setNoMeasurementsMessage();
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
															
															Element chart = DOM.createDiv();
															chart.setId("measurements");
															chart.getStyle().setHeight(250, Unit.PX);
															chart.getStyle().setWidth(600, Unit.PX);
															chart.getStyle().setBackgroundColor("white");
															
															FlowPanel panel = new FlowPanel();
															panel.getElement().appendChild(chart);
															view.setPlot(panel);
															
															String unit = parameter.getMeasurementTypeUnit() == null ? "unknown" :
																	parameter.getMeasurementTypeUnit();
															String unitLabel = parameter.getMeasurementTypeName() == null ? "unknown" :
																	parameter.getMeasurementTypeName();
															graph = showDygraphChart(getDygraphValues(measurements, unitLabel), unitLabel + ", " + unit,
																	unitLabel + " (" + deviceId + ")");
														}
													}
												});
											} else {
												view.showMessageLabel(true);
												view.setNoTimelinesMessage();
											}
										}
									});
								} else {
									view.showMessageLabel(true);
									view.setNoContextsMessage();
								}
							}
						});
					} else {
						view.showMessageLabel(true);
						view.setNoParamtersMessage();
					}
				}
			});
		}
	}
	
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
	
	private native void push(double value, String timestamp, JavaScriptObject values) /*-{
		values.push({value: value, timestamp: timestamp});
	}-*/;
	
	private native JavaScriptObject showDygraphChart(String values, String yLabel, String title) /*-{
		return new $wnd.Dygraph($doc.getElementById('measurements'), values, {
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
	
	private native void updateDygraphData(String data) /*-{
		var graph = this.@pl.ismop.web.client.widgets.plot.PlotPresenter::graph;
		
		if(graph) {
			this.@pl.ismop.web.client.widgets.plot.PlotPresenter::graph.updateOptions({
				file: data
			});
		}
	}-*/;
}