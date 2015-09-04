package pl.ismop.web.client.widgets.old.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Legend.Layout;
import org.moxieapps.gwt.highcharts.client.Legend.VerticalAlign;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.StockChart;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
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
import pl.ismop.web.client.widgets.old.plot.Readings;
import pl.ismop.web.client.widgets.old.weather.GroupedReadings.LatestReading;
import pl.ismop.web.client.widgets.old.weather.IWeatherStationView.IWeatherStationPresenter;

@Presenter(view = WeatherStationView.class)
public class WeatherStationPresenter extends BasePresenter<IWeatherStationView, MainEventBus> implements IWeatherStationPresenter {
	private DapController dapController;
	private StockChart chart;

	@Inject
	public WeatherStationPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowWeatherStation() {
		view.getChartVisibility().setVisible(false);
		view.showModal();
		loadParameters();
	}

	private void loadParameters() {
		view.getContentVisibility().setVisible(false);
		view.showProgress(true);
		view.clearMeasurements();
		view.getChartVisibility().setVisible(false);
		dapController.getDevicesForType("weather_station", new DevicesCallback() {
			@Override
			public void onError(int code, String message) {
				view.showProgress(false);
				Window.alert(message);
			}
			
			@Override
			public void processDevices(List<Device> devices) {
				if(devices.size() > 0) {
					groupAndProcessReadings(devices);
				} else {
					view.showProgress(false);
				}
			}
		});
	}
	
	private void groupAndProcessReadings(List<Device> devices) {
		final List<String> deviceIds = new ArrayList<>();
		final Map<String, Device> deviceMap = new HashMap<>();
		
		for(Device device : devices) {
			deviceIds.add(device.getId());
			deviceMap.put(device.getId(), device);
		}
		
		dapController.getParameters(deviceIds, new ParametersCallback() {
			@Override
			public void onError(int code, String message) {
				view.showProgress(false);
				Window.alert(message);
			}
			
			@Override
			public void processParameters(List<Parameter> parameters) {
				if(parameters.size() > 0) {
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
								dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds, new TimelinesCallback() {
									@Override
									public void onError(int code, String message) {
										view.showProgress(false);
										Window.alert(message);
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
											
											dapController.getMeasurementsForTimelineIds(timelineIds, new MeasurementsCallback() {
												@Override
												public void onError(int code, String message) {
													view.showProgress(false);
													Window.alert(message);
												}
												
												@Override
												public void processMeasurements(List<Measurement> measurements) {
													view.showProgress(false);
													
													GroupedReadings groupedReadings = groupReadings(measurements, timelineMap, parameterMap, deviceMap);
													updateView(groupedReadings);
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
						Date date = format.parse(measurement.getTimestamp());
						values[index][0] = date.getTime();
						values[index][1] = measurement.getValue();
						
						if(index == measurementCount - 1) {
							//last measurement
							Device device = findDevice(deviceMap, parameter.getId());
							LatestReading latestReading = new LatestReading();
							latestReading.label = parameter.getMeasurementTypeName();
							latestReading.timestamp = date;
							latestReading.unit = parameter.getMeasurementTypeUnit();
							latestReading.value = measurement.getValue();
							
							if(groupedReadings.getLatestReadings().get(device.getCustomId()) == null) {
								groupedReadings.getLatestReadings().put(device.getCustomId(), new ArrayList<LatestReading>());
							}
							
							groupedReadings.getLatestReadings().get(device.getCustomId()).add(latestReading);
						}
						
						index++;
					}
				}
				
				readings.getMeasurements().put(parameter.getParamterName(), values);
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
			if(readings.getLabel().equals(parameter.getMeasurementTypeName()) && readings.getUnit().equals(parameter.getMeasurementTypeUnit())) {
				return readings;
			}
		}
		
		Readings readings = new Readings();
		readings.setLabel(parameter.getMeasurementTypeName());
		readings.setUnit(parameter.getMeasurementTypeUnit());
		readings.setMeasurements(new HashMap<String, Number[][]>());
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
	
	private void updateView(GroupedReadings groupedReadings) {
		Iterator<String> keyIterator = groupedReadings.getLatestReadings().keySet().iterator();
		
		if(groupedReadings.getLatestReadings().keySet().size() > 0) {
			view.getContentVisibility().setVisible(true);
			
			String stationName = keyIterator.next();
			view.getHeading1().setText(stationName);
			sortLatestReadings(groupedReadings.getLatestReadings().get(stationName));
			
			for(LatestReading latestReading : groupedReadings.getLatestReadings().get(stationName)) {
				view.addLatestReading1(latestReading.label, NumberFormat.getFormat("0.00").format(latestReading.value), latestReading.unit,
						DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(latestReading.timestamp));
			}
		}
		
		if(groupedReadings.getLatestReadings().keySet().size() > 1) {
			String stationName = keyIterator.next();
			view.getHeading2().setText(stationName);
			sortLatestReadings(groupedReadings.getLatestReadings().get(stationName));
			
			for(LatestReading latestReading : groupedReadings.getLatestReadings().get(stationName)) {
				view.addLatestReading2(latestReading.label, NumberFormat.getFormat("0.00").format(latestReading.value), latestReading.unit,
						DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(latestReading.timestamp));
			}
		}
		
		if(chart != null) {
			chart.removeAllSeries();
			chart.removeFromParent();
		}
		
		chart = new StockChart()
				.setType(Series.Type.LINE)
				.setLegend(new Legend()
						.setVerticalAlign(VerticalAlign.BOTTOM)
						.setLayout(Layout.HORIZONTAL))
				.setToolTip(new ToolTip()
						.setPointFormat("<span style=\"color: {point.color};\">\u25CF</span> {series.name}: <b>{point.y:.2f}</b><br/>"));
		
		int axisIndex = 0;
		
		for(final Readings readingsEntry : groupedReadings.getReadingsList()) {
			chart.getYAxis(axisIndex)
				.setAxisTitle(new AxisTitle().setText(readingsEntry.getLabel() + " [" + readingsEntry.getUnit() + "]"))
				.setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
					@Override
					public String format(AxisLabelsData axisLabelsData) {
						return NumberFormat.getFormat("0.00").format(axisLabelsData.getValueAsDouble());
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
		
		view.getChartVisibility().setVisible(true);
		view.setChart(chart);
	}

	private void sortLatestReadings(List<LatestReading> readings) {
		Collections.sort(readings, new Comparator<LatestReading>() {
			@Override
			public int compare(LatestReading o1, LatestReading o2) {
				return o1.label.compareTo(o2.label);
			}
		});
	}
}