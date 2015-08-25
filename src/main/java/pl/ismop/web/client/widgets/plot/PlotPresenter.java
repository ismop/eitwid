package pl.ismop.web.client.widgets.plot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.StockChart;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ContextsCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.ParametersCallback;
import pl.ismop.web.client.dap.DapController.TimelinesCallback;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.plot.IPlotView.IPlotPresenter;

@Presenter(view = PlotView.class, multiple = true)
public class PlotPresenter extends BasePresenter<IPlotView, MainEventBus> implements IPlotPresenter {
	private DapController dapController;
	private Map<String, Parameter> parameters;
	private Map<String, Timeline> timelines;
	private StockChart chart;

	@Inject
	public PlotPresenter(DapController dapController) {
		this.dapController = dapController;
		parameters = new HashMap<>();
		timelines = new HashMap<>();
	}
	
	public void drawMeasurements(final Map<String, Device> devices) {
		parameters.clear();
		timelines.clear();
		view.showMessageLabel(false);
		view.showBusyPanel(true);
		
		if(chart != null) {
			chart.removeAllSeries();
			chart.removeFromParent();
			chart = null;
		}
		
		if(devices.size() > 0) {
			dapController.getParameters(new ArrayList<String>(devices.keySet()), new ParametersCallback() {
				@Override
				public void onError(int code, String message) {
					view.showBusyPanel(false);
					Window.alert(message);
				}
				
				@Override
				public void processParameters(List<Parameter> parameters) {
					if(parameters.size() > 0) {
						for(Parameter parameter : parameters) {
							PlotPresenter.this.parameters.put(parameter.getId(), parameter);
						}
						
						dapController.getContext("measurements", new ContextsCallback() {
							@Override
							public void onError(int code, String message) {
								view.showBusyPanel(false);
								Window.alert(message);
							}
							
							@Override
							public void processContexts(List<Context> contexts) {
								if(contexts.size() > 0) {
									//there should be only one context
									Context context = contexts.get(0);
									dapController.getTimelinesForParameterIds(context.getId(), new ArrayList<String>(PlotPresenter.this.parameters.keySet()),
											new TimelinesCallback() {
												@Override
												public void onError(int code, String message) {
													view.showBusyPanel(false);
													Window.alert(message);
												}
												
												@Override
												public void processTimelines(List<Timeline> timelines) {
													if(timelines.size() > 0) {
														for(Timeline timeline : timelines) {
															PlotPresenter.this.timelines.put(timeline.getId(), timeline);
														}
														
														dapController.getMeasurementsForTimelineIds(new ArrayList<String>(PlotPresenter.this.timelines.keySet()),
																new MeasurementsCallback() {
															@Override
															public void onError(int code, String message) {
																view.showBusyPanel(false);
																Window.alert(message);
															}
															
															@Override
															public void processMeasurements(List<Measurement> measurements) {
																view.showBusyPanel(false);
																
																if(measurements.size() == 0) {
																	view.showMessageLabel(true);
																	view.setNoMeasurementsMessage();
																} else {
																	if(chart != null) {
																		chart.removeAllSeries();
																		chart.removeFromParent();
																		chart = null;
																	}
																	
																	List<Readings> readings = createReadings(devices, PlotPresenter.this.parameters,
																			PlotPresenter.this.timelines, measurements);
																	chart = new StockChart().setType(Series.Type.LINE);
																	
																	int axisIndex = 0;
																	
																	for(final Readings readingsEntry : readings) {
																		chart.getYAxis(axisIndex)
																			.setAxisTitle(new AxisTitle().setText(readingsEntry.getLabel()))
																			.setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
																				@Override
																				public String format(AxisLabelsData axisLabelsData) {
																					return axisLabelsData.getValueAsString() + " " + readingsEntry.getUnit();
																				}
																			}));
																		
																		for(String deviceCustomId : readingsEntry.getMeasurements().keySet()) {
																			chart.addSeries(chart.createSeries()
																					.setName(deviceCustomId)
																					.setYAxis(axisIndex)
																					.setPoints(readingsEntry.getMeasurements().get(deviceCustomId)));
																		}
																		
																		axisIndex++;
																	}
																	
																	view.setPlot(chart);
																}
															}
														});
													} else {
														view.showBusyPanel(false);
														view.showMessageLabel(true);
														view.setNoTimelinesMessage();
													}
												}
									});
								} else {
									view.showBusyPanel(false);
									view.showMessageLabel(true);
									view.setNoContextsMessage();
								}
							}
						});
					} else {
						view.showBusyPanel(false);
						view.showMessageLabel(true);
						view.setNoParamtersMessage();
					}
				}
			});
		}
	}
	
	private List<Readings> createReadings(Map<String, Device> devices, Map<String, Parameter> parameters, Map<String, Timeline> timelines,
			List<Measurement> measurements) {
		List<Readings> result = new ArrayList<>();
		Map<String, List<Parameter>> matchedParameters = new HashMap<>();
		
		for(String parameterId : parameters.keySet()) {
			String key = parameters.get(parameterId).getMeasurementTypeName() + parameters.get(parameterId).getMeasurementTypeUnit();
			
			if(matchedParameters.get(key) == null) {
				matchedParameters.put(key, new ArrayList<Parameter>());
			}
			
			matchedParameters.get(key).add(parameters.get(parameterId));
		}
		
		for(List<Parameter> parameterGroup : matchedParameters.values()) {
			Readings readings = new Readings();
			readings.setMeasurements(new HashMap<String, Number[][]>());
			readings.setLabel(parameterGroup.get(0).getMeasurementTypeName());
			readings.setUnit(parameterGroup.get(0).getMeasurementTypeUnit());
			
			for(Parameter parameter : parameterGroup) {
				String customDeviceId = devices.get(parameter.getDeviceId()).getCustomId();
				//TODO: use all timelines
				Timeline timeline = timelines.get(parameter.getTimelineIds().get(0));
				Number[][] measurementValues = new Number[countMeasurements(timeline.getId(), measurements)][2];
				int index = 0;
				
				for(Measurement measurement : measurements) {
					if(measurement.getTimelineId().equals(timeline.getId())) {
						DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
						Date date = format.parse(measurement.getTimestamp());
						measurementValues[index][0] = date.getTime();
						measurementValues[index][1] = measurement.getValue();
						index++;
					}
				}
				
				readings.getMeasurements().put(customDeviceId, measurementValues);
			}
			
			result.add(readings);
		}
		
		return result;
	}

	private int countMeasurements(String timelineId, List<Measurement> measurements) {
		int result = 0;
		
		for(Measurement measurement : measurements) {
			if(measurement.getTimelineId().equals(timelineId)) {
				result++;
			}
		}
		
		return result;
	}
}