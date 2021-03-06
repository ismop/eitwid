package pl.ismop.web.client.widgets.monitoring.weather;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopConverter;
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
import pl.ismop.web.client.util.TimelineZoomDataCallbackHelper;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.monitoring.weather.IWeatherStationView.IWeatherStationPresenter;

@Presenter(view = WeatherStationView.class)
public class WeatherStationPresenter extends BasePresenter<IWeatherStationView, MainEventBus>
		implements IWeatherStationPresenter {
	private DapController dapController;
	
	private ChartPresenter chartPresenter;
	
	final WeatherReadings readings = new WeatherReadings();

	private IsmopConverter ismopConverter;
	
	@Inject
	public WeatherStationPresenter(DapController dapController, IsmopConverter ismopConverter) {
		this.dapController = dapController;
		this.ismopConverter = ismopConverter;
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
	
	@Override
	public void loadParameter(String parameterId, Boolean value) {
		Parameter parameter = readings.parameterMap.get(parameterId);
		
		if (parameter != null) {
			if (value){
				loadParameter(parameter);
			} else {
				unloadParameter(parameter);
			}
		}
	}

	private void initPresenter() {
		view.getContentVisibility().setVisible(false);
		
		if (chartPresenter == null) {
			chartPresenter = eventBus.addHandler(ChartPresenter.class);
			chartPresenter.setHeight(view.getChartContainerHeight());
			chartPresenter.setZoomDataCallback(new TimelineZoomDataCallbackHelper(dapController,
					eventBus, chartPresenter));
			view.setChart(chartPresenter.getView());
		}
		
		chartPresenter.reset();	
		view.showProgress(true);
		view.clearMeasurements();
		readings.clear(); 
		preloadParametersWithLatestReadings();
	}
	
	private void unloadParameter(Parameter parameter) {
		chartPresenter.removeChartSeriesForParameter(parameter);
	}

	private void loadParameter(final Parameter parameter) {
		chartPresenter.setLoadingState(true);
		
		Timeline timeline = readings.parameterToTimeline.get(parameter.getId());
		
		if (timeline!= null) {
			Date now = new Date(); 
			Date twoWeeksAgo = CalendarUtil.copyDate(now);
			CalendarUtil.addDaysToDate(twoWeeksAgo, -14);
			dapController.getMeasurementsWithQuantity(timeline.getId(), 1000,
					new MeasurementsCallback() {
				@Override
				public void onError(ErrorDetails errorDetails) {
					chartPresenter.setLoadingState(false);
					showError(errorDetails);
				}
				
				@Override
				public void processMeasurements(List<Measurement> measurements) {
					chartPresenter.setLoadingState(false);
					chartPresenter.addChartSeries(series(parameter, measurements));
				}
			});
		} else {
			chartPresenter.setLoadingState(false);
		}

	}
	
	private void preloadParametersWithLatestReadings() {
		dapController.getDevicesForType("weather_station", new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showProgress(false);
				eventBus.showSimpleError("Error: " + errorDetails.getMessage());
			}

			@Override
			public void processDevices(List<Device> devices) {
				if(devices.size() > 0) {
					readings.addDevices(devices);
					dapController.getParameters(readings.deviceIds, new ParametersCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showProgress(false);
							showError(errorDetails);
						}

						@Override
						public void processParameters(List<Parameter> parameters) {
							if(parameters.size() > 0) {
								readings.addParemeters(parameters);
								dapController.getContext("measurements", new ContextsCallback() {
									
									@Override
									public void onError(ErrorDetails errorDetails) {
										view.showProgress(false);
										showError(errorDetails);
									}
									
									@Override
									public void processContexts(List<Context> contexts) {
										if(contexts.size() > 0) {
											readings.setContext(contexts.get(0));
											dapController.getTimelinesForParameterIds(
													readings.context.getId(),
													readings.parameterIds,
													new TimelinesCallback() {
												
												@Override
												public void onError(ErrorDetails errorDetails) {
													view.showProgress(false);
													showError(errorDetails);
												}
												
												@Override
												public void processTimelines(
														List<Timeline> timelines) {
													if(timelines.size() > 0) {
														readings.addTimelines(timelines);
														dapController.getLastMeasurementsWith24HourMod(
																readings.timelineIds,
																new Date(),
																new MeasurementsCallback() {
															@Override
															public void onError(
																	ErrorDetails errorDetails) {
																view.showProgress(false);
																showError(errorDetails);
															}
															
															@Override
															public void processMeasurements(
																	List<Measurement> measurements) {
																updateParamPreview(measurements);
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

	private void updateParamPreview(List<Measurement> measurements) {
		Map<String, Measurement> lastMeasurements = readings.getLastMeasurements(measurements);
	
		if (readings.deviceIds.size() > 0) {
			Device device = readings.deviceMap.get(readings.deviceIds.get(0));
			view.getHeading1().setText(device.getCustomId());
			
			List<Parameter> parameters = readings.getParametersForDevice(device.getId());
			
			for (Parameter parameter : parameters) {
				if (lastMeasurements.get(parameter.getId()) != null) {
					view.addLatestReading1(parameter.getId(), 
							parameter.getParameterName(), parameter.getMeasurementTypeName(),
							NumberFormat.getFormat("0.00").format(
									lastMeasurements.get(parameter.getId()).getValue()),
										parameter.getMeasurementTypeUnit(),
										ismopConverter.formatForDisplay(
												lastMeasurements.get(
														parameter.getId()).getTimestamp()));
				} else {
					view.addLatestReading1(
						parameter.getId(), 
						parameter.getParameterName(),
						parameter.getMeasurementTypeName(),
						view.getNoReadingLabel(),
						"",
						"");	
				}
			}
		} 
		
		if (readings.deviceIds.size() > 1) {
			Device device = readings.deviceMap.get(readings.deviceIds.get(1));
			view.getHeading2().setText(device.getCustomId());
			List<Parameter> parameters = readings.getParametersForDevice(device.getId());
			for (Parameter parameter : parameters) {
				if (lastMeasurements.get(parameter.getId()) != null) {
					view.addLatestReading2(parameter.getId(), 
							parameter.getParameterName(), parameter.getMeasurementTypeName(),
							NumberFormat.getFormat("0.00").format(
									lastMeasurements.get(parameter.getId()).getValue()),
							parameter.getMeasurementTypeUnit(),
							ismopConverter.formatForDisplay(
									lastMeasurements.get(parameter.getId()).getTimestamp()));
				} else {
					view.addLatestReading2(
						parameter.getId(), 
						parameter.getParameterName(),
						parameter.getMeasurementTypeName(),
						view.getNoReadingLabel(),
						"",
						"");
				}
			}
		} 
		
		view.getContentVisibility().setVisible(true);
	}
	
	private ChartSeries series(Parameter parameter, List<Measurement> measurements) {
		Device device = readings.deviceMap.get(parameter.getDeviceId());
		ChartSeries s1 = new ChartSeries();
		s1.setName(device.getCustomId() + " - " + parameter.getParameterName());
		s1.setDeviceId(parameter.getDeviceId());
		s1.setParameterId(parameter.getId());
		s1.setUnit(parameter.getMeasurementTypeUnit());
		s1.setLabel(parameter.getMeasurementTypeName());
		
		if (measurements.size() > 0) {
			s1.setTimelineId(measurements.get(0).getTimelineId());
		}
		
		Number[][] values = new Number[measurements.size()][2];
		
		for (int j = 0; j < measurements.size(); j++) {
			Measurement measurement = measurements.get(j);
			values[j][0] = measurement.getTimestamp().getTime();
			values[j][1] = measurement.getValue();
		}
		
		s1.setValues(values);
		
		return s1;
	}
	
	private void showError(ErrorDetails errorDetails) {
		eventBus.showSimpleError("Error: " + errorDetails.getMessage());
	}
}