package pl.ismop.web.client.widgets.weather;

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
import com.google.gwt.user.client.ui.HasText;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ContextsCallback;
import pl.ismop.web.client.dap.DapController.DevicesCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.ParametersCallback;
import pl.ismop.web.client.dap.DapController.TimelinesCallback;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.plot.Readings;
import pl.ismop.web.client.widgets.weather.IWeatherStationView.IWeatherStationPresenter;

@Presenter(view = WeatherStationView.class)
public class WeatherStationPresenter extends BasePresenter<IWeatherStationView, MainEventBus> implements IWeatherStationPresenter {
	private DapController dapController;
	private StockChart secondChart;
	private StockChart firstChart;

	@Inject
	public WeatherStationPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowWeatherStation() {
		view.clearMessage();
		view.showDataContainer(false);
		view.showModal();
		loadParameters();
	}

	private void loadParameters() {
		view.showProgress(true);
		dapController.getDevicesForType("weather_station", new DevicesCallback() {
			@Override
			public void onError(int code, String message) {
				view.showProgress(false);
				Window.alert(message);
			}
			
			@Override
			public void processDevices(List<Device> devices) {
				if(devices.size() > 0) {
					Device firstDevice = devices.get(0);
					Device secondDevice = devices.size() > 1 ? devices.get(1) : null;
					
					if(firstChart != null) {
						firstChart.removeAllSeries();
						firstChart.removeFromParent();
						firstChart = null;
					}
					
					firstChart = new StockChart().setType(Series.Type.LINE);
					view.setFirstChart(firstChart);
					showDataSet(firstDevice, view.getFirstHeading(), firstChart);
					
					if(secondDevice != null) {
						if(secondChart != null) {
							secondChart.removeAllSeries();
							secondChart.removeFromParent();
							secondChart = null;
						}
						
						secondChart = new StockChart().setType(Series.Type.LINE);
						view.setSecondChart(secondChart);
						showDataSet(secondDevice, view.getSecondHeading(), secondChart);
					}
					
				} else {
					view.showProgress(false);
					view.showNoWeatherStationData(true);
				}
			}
		});
	}
	
	private void showDataSet(final Device device, final HasText heading, final StockChart chart) {
		dapController.getParameters(device.getId(), new ParametersCallback() {
			@Override
			public void onError(int code, String message) {
				view.showProgress(false);
				Window.alert(message);
			}
			
			@Override
			public void processParameters(List<Parameter> parameters) {
				final List<String> parameterIds = new ArrayList<>();
				final Map<String, Parameter> parameterMap = new HashMap<>();
				
				for(Parameter parameter : parameters) {
					parameterMap.put(parameter.getId(), parameter);
					parameterIds.add(parameter.getId());
				}
				
				dapController.getContext("measurements", new ContextsCallback() {
					@Override
					public void onError(int code, String message) {
						view.showProgress(false);
						Window.alert(message);
					}
					
					@Override
					public void processContexts(List<Context> contexts) {
						if(contexts.size() > 0) {
							Context context = contexts.get(0);
							dapController.getTimelinesForParameterIds(context.getId(), parameterIds, new TimelinesCallback() {
								@Override
								public void onError(int code, String message) {
									view.showProgress(false);
									Window.alert(message);
								}
								
								@Override
								public void processTimelines(List<Timeline> timelines) {
									List<String> timelineIds = new ArrayList<>();
									final Map<String, Timeline> timelineMap = new HashMap<>();
									
									for(Timeline timeline : timelines) {
										timelineIds.add(timeline.getId());
										timelineMap.put(timeline.getId(), timeline);
									}
									
									dapController.getMeasurementsForTimelineIds(timelineIds, new MeasurementsCallback() {
										@Override
										public void onError(int code, String message) {
											view.showProgress(false);
											Window.alert(message);
										}
										
										@Override
										public void processMeasurements(List<Measurement> measurements) {
											view.showProgress(false);
											
											if(measurements.size() > 0) {
												view.showDataContainer(true);
												heading.setText(device.getCustomId());
												List<Readings> readingsList = sortMeasurements(measurements, parameterMap, timelineMap);
												
												int axisIndex = 0;
												
												for(final Readings readingsEntry : readingsList) {
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
											}
											
										}
									});
								}
							});
						} else {
							view.showProgress(false);
							Window.alert("No valid context found");
						}
					}
				});
			}
		});
		heading.setText(device.getCustomId());
	}
	
	private List<Readings> sortMeasurements(List<Measurement> measurements, Map<String, Parameter> parameterMap, Map<String, Timeline> timelineMap) {
		List<Readings> result = new ArrayList<>();
		
		for(String parameterId : parameterMap.keySet()) {
			Parameter parameter = parameterMap.get(parameterId);
			Readings readings = new Readings();
			result.add(readings);
			readings.setMeasurements(new HashMap<String, Number[][]>());
			readings.setLabel(parameter.getMeasurementTypeName());
			readings.setUnit(parameter.getMeasurementTypeUnit());
			
			Timeline timeline = null;
			
			for(String timelineId : timelineMap.keySet()) {
				if(timelineMap.get(timelineId).getParameterId().equals(parameter.getId())) {
					timeline = timelineMap.get(timelineId);
					
					break;
				}
			}
			
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
			
			readings.getMeasurements().put(parameter.getParamterName(), measurementValues);
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