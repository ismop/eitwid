package pl.ismop.web.client.widgets.monitoring.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.monitoring.weather.GroupedReadings.LatestReading;
import pl.ismop.web.client.widgets.monitoring.weather.IWeatherStationView.IWeatherStationPresenter;
import pl.ismop.web.client.widgets.old.plot.Readings;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = WeatherStationView.class)
public class WeatherStationPresenter extends BasePresenter<IWeatherStationView, MainEventBus> implements IWeatherStationPresenter {
	
	private DapController dapController;
	
	private ChartPresenter chartPresenter;

	final Map<String, Parameter> parameterMap = new HashMap<>();
	
	@Inject
	public WeatherStationPresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void onShowWeatherPanel() {
		view.showModal();
	}
	
	@Override
	public void onModalShown() {
		initPresenter();
	}
	
	@Override
	public void onModalHidden() {
		view.getContentVisibility().setVisible(false);
		chartPresenter.reset();
	}
	
	private void initPresenter() {
		view.getContentVisibility().setVisible(false);
		if(chartPresenter == null) {
			chartPresenter = eventBus.addHandler(ChartPresenter.class);
			chartPresenter.setHeight(view.getChartContainerHeight());
			chartPresenter.addSeriesHoverListener();
			view.setChart(chartPresenter.getView());
		}
		chartPresenter.reset();	
		view.showProgress(true);
		view.clearMeasurements();
		parameterMap.clear(); 
		preloadParametersWithLatestReadings();
	}
	
	@Override
	public void loadParameter(String parameterId, Boolean value) {
		Parameter parameter = parameterMap.get(parameterId);
		if (parameter != null) {
			if (value){
				view.showProgress(true);
				loadParameter(parameter);
			} else {
				unloadParameter(parameter);
			}
		}
	}
		
	private void unloadParameter(Parameter parameter) {
		chartPresenter.removeChartSeriesForParameter(parameter);
	}

	private void loadParameter(final Parameter parameter) {
		final List<String> parameterIds = new ArrayList<>();
		parameterIds.add(parameter.getId());
		dapController.getContext("measurements", new ContextsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showProgress(false);
				Window.alert("Error: " + errorDetails.getMessage());
			}
			
			@Override
			public void processContexts(List<Context> contexts) {
				if(contexts.size() > 0) {
					dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds, new TimelinesCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showProgress(false);
							Window.alert("Error: " + errorDetails.getMessage());
						}
						
						@Override
						public void processTimelines(List<Timeline> timelines) {
							if(timelines.size() > 0) {
								Date now = new Date(); 
								Date twoWeeksAgo = CalendarUtil.copyDate(now);
								CalendarUtil.addDaysToDate(twoWeeksAgo, -14);
								dapController.getMeasurements(timelines.get(0).getId(), twoWeeksAgo, now, new MeasurementsCallback() {
									@Override
									public void onError(ErrorDetails errorDetails) {
										view.showProgress(false);
										Window.alert("Error: " + errorDetails.getMessage());
									}
									
									@Override
									public void processMeasurements(List<Measurement> measurements) {
										chartPresenter.addChartSeries(series(parameter, measurements));
										view.showProgress(false);
									}
								});
							} else {
								view.showProgress(false);
							}
						}
					});
				} else {
					view.showProgress(false);
				}
			}
		});
	}

	private void preloadParametersWithLatestReadings() {
		dapController.getDevicesForType("weather_station", new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showProgress(false);
				Window.alert("Error: " + errorDetails.getMessage());
			}

			@Override
			public void processDevices(List<Device> devices) {
				if(devices.size() > 0) {
					final List<String> deviceIds = new ArrayList<>();
					final Map<String, Device> deviceMap = new HashMap<>();
					
					for(Device device : devices) {
						deviceIds.add(device.getId());
						deviceMap.put(device.getId(), device);
					}
					
					dapController.getParameters(deviceIds, new ParametersCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showProgress(false);
							Window.alert("Error: " + errorDetails.getMessage());
						}
						
						@Override
						public void processParameters(List<Parameter> parameters) {
							if(parameters.size() > 0) {
								final List<String> parameterIds = new ArrayList<>();
								// Recreate parameter map - should be empty
								for(Parameter parameter : parameters) {
									parameterMap.put(parameter.getId(), parameter);
									parameterIds.add(parameter.getId());
								}
								
								dapController.getContext("measurements", new ContextsCallback() {
									@Override
									public void onError(ErrorDetails errorDetails) {
										view.showProgress(false);
										Window.alert("Error: " + errorDetails.getMessage());
									}
									
									@Override
									public void processContexts(List<Context> contexts) {
										if(contexts.size() > 0) {
											dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds, new TimelinesCallback() {
												@Override
												public void onError(ErrorDetails errorDetails) {
													view.showProgress(false);
													Window.alert("Error: " + errorDetails.getMessage());
												}
												
												@Override
												public void processTimelines(List<Timeline> timelines) {
													if(timelines.size() > 0) {
														final List<String> timelineIds = new ArrayList<>();
														final Map<String, Timeline> timelineMap = new HashMap<>();
														for(Timeline timeline : timelines) {
															timelineIds.add(timeline.getId());
															timelineMap.put(timeline.getId(), timeline);
														}
														Date lastDay = new Date();
														CalendarUtil.addDaysToDate(lastDay, -1);
														dapController.getLastMeasurements(timelineIds, lastDay, new MeasurementsCallback() {
															@Override
															public void onError(ErrorDetails errorDetails) {
																view.showProgress(false);
																Window.alert("Error: " + errorDetails.getMessage());
															}
															
															@Override
															public void processMeasurements(List<Measurement> measurements) {
																GroupedReadings groupedReadings = groupReadings(measurements, timelineMap, parameterMap, deviceMap);
																updateParamPreview(groupedReadings);
															}
			
														});
													} else {
														view.showProgress(false);
													}
												}
											});
										} else {
											view.showProgress(false);
										}
									}
								});
							} else {
								view.showProgress(false);
							}
						}
					});
				} else {
					view.showProgress(false);
				}
			}
		});	
	}
	
	private GroupedReadings groupReadings(List<Measurement> measurements, Map<String, Timeline> timelineMap, Map<String, Parameter> parameterMap,
			Map<String, Device> deviceMap) {
		GroupedReadings groupedReadings = new GroupedReadings();
		groupedReadings.setReadingsList(new ArrayList<Readings>());
		groupedReadings.setLatestReadings(new HashMap<String, List<LatestReading>>());
		
		for(String parameterId : parameterMap.keySet()) {
			Parameter parameter = parameterMap.get(parameterId);
			Readings readings = findOrCreateSimilarReadings(parameter, groupedReadings.getReadingsList());
			String timelineId = parameter.getTimelineIds().get(0);
			int measurementCount = countMeasurements(timelineId, measurements);
			
			if(measurementCount > 0) {
				Number[][] values = new Number[measurementCount][2];
				int index = 0;
				
				for(Measurement measurement : measurements) {
					if(timelineId.equals(measurement.getTimelineId())) {
						DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
						Date date = measurement.getTimestamp();
						values[index][0] = date.getTime();
						values[index][1] = measurement.getValue();
						
						if(index == measurementCount - 1) {
							//last measurement
							Device device = findDevice(deviceMap, parameter.getId());
							LatestReading latestReading = new LatestReading();
							latestReading.parameterName = parameter.getParameterName();
							latestReading.typeName = parameter.getMeasurementTypeName();
							latestReading.timestamp = date;
							latestReading.unit = parameter.getMeasurementTypeUnit();
							latestReading.value = measurement.getValue();
							latestReading.parameterId = parameter.getId();
							
							if(groupedReadings.getLatestReadings().get(device.getCustomId()) == null) {
								groupedReadings.getLatestReadings().put(device.getCustomId(), new ArrayList<LatestReading>());
							}
							
							groupedReadings.getLatestReadings().get(device.getCustomId()).add(latestReading);
						}
						
						index++;
					}
				}
				
				readings.getMeasurements().put(parameter.getParameterName(), values);
			}
		}
		
		return groupedReadings;
	}

	private Device findDevice(Map<String, Device> deviceMap, String parameterId) {
		for(String deviceId : deviceMap.keySet()) {
			if(deviceMap.get(deviceId).getParameterIds().contains(parameterId)) {
				return deviceMap.get(deviceId);
			}
		}
		return null;
	}

	private Readings findOrCreateSimilarReadings(Parameter parameter, List<Readings> readingsList) {
		for(Readings readings : readingsList) {
			if (readings.getParameterId().equals(parameter.getId())) {
				return readings;
			}
//			if(readings.getLabel().equals(parameter.getMeasurementTypeName()) && readings.getUnit().equals(parameter.getMeasurementTypeUnit())) {
//				return readings;
//			}
		}
		Readings readings = new Readings();
		readings.setParameterName(parameter.getParameterName());
		readings.setTypeName(parameter.getMeasurementTypeName());
		readings.setUnit(parameter.getMeasurementTypeUnit());
		readings.setMeasurements(new HashMap<String, Number[][]>());
		readings.setParameterId(parameter.getId());
		readingsList.add(readings);
		
		return readings;
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
	

	private ChartSeries series(Parameter parameter, List<Measurement> measurements) {
		ChartSeries s1 = new ChartSeries();
		s1.setName(parameter.getParameterName());
		s1.setDeviceId(parameter.getDeviceId());
		s1.setParameterId(parameter.getId());
		s1.setUnit(parameter.getMeasurementTypeUnit());
		s1.setLabel(parameter.getMeasurementTypeName());
		Number[][] values = new Number[measurements.size()][2];
		for(int j = 0; j<measurements.size(); j++) {
			Measurement measurement = measurements.get(j);
			DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
			Date date = measurement.getTimestamp();
			values[j][0] = date.getTime();
			values[j][1] = measurement.getValue();
		}
		s1.setValues(values);
		return s1;
	}

	private void updateParamPreview(GroupedReadings groupedReadings) {
		Iterator<String> keyIterator = groupedReadings.getLatestReadings().keySet().iterator();
		String firstParamId = null;
		if(groupedReadings.getLatestReadings().keySet().size() > 0) {
			view.getContentVisibility().setVisible(true);
			
			String stationName = keyIterator.next();
			view.getHeading1().setText(stationName);
			sortLatestReadings(groupedReadings.getLatestReadings().get(stationName));
			
			for(LatestReading latestReading : groupedReadings.getLatestReadings().get(stationName)) {
				if (firstParamId == null) {
					firstParamId = latestReading.parameterId;
				}
				view.addLatestReading1(latestReading.parameterId, 
						latestReading.parameterName, latestReading.typeName, NumberFormat.getFormat("0.00").format(latestReading.value), latestReading.unit,
						DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(latestReading.timestamp));
			}
		}
		
		if(groupedReadings.getLatestReadings().keySet().size() > 1) {
			String stationName = keyIterator.next();
			view.getHeading2().setText(stationName);
			sortLatestReadings(groupedReadings.getLatestReadings().get(stationName));
			
			for(LatestReading latestReading : groupedReadings.getLatestReadings().get(stationName)) {
				view.addLatestReading2(latestReading.parameterId, latestReading.parameterName, latestReading.typeName, NumberFormat.getFormat("0.00").format(latestReading.value), latestReading.unit,
						DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(latestReading.timestamp));
			}
		}

		view.getContentVisibility().setVisible(true);
	}

	private void sortLatestReadings(List<LatestReading> readings) {
		Collections.sort(readings, new Comparator<LatestReading>() {
			@Override
			public int compare(LatestReading o1, LatestReading o2) {
				return o1.parameterName.compareTo(o2.parameterName);
			}
		});
	}

}