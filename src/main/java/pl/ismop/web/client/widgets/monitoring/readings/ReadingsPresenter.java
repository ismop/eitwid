package pl.ismop.web.client.widgets.monitoring.readings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ContextsCallback;
import pl.ismop.web.client.dap.DapController.DevicesCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.ParametersCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.DapController.TimelinesCallback;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.util.TimelineZoomDataCallbackHelper;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.monitoring.readings.IReadingsView.IReadingsPresenter;

@Presenter(view = ReadingsView.class)
public class ReadingsPresenter extends BasePresenter<IReadingsView, MainEventBus>
		implements IReadingsPresenter {

	private static final String PICK_VALUE = "pick";

	private DapController dapController;

	private MapPresenter mapPresenter;

	private ChartPresenter chartPresenter ;

	private Levee levee;

	private List<ChartSeries> series;

	private Map<String, Device> displayedDevices;

	private Map<String, Parameter> additionalParameters;

	private List<String> chosenAdditionalReadings;

	private Map<String, Device> additionalDevices;

	@Inject
	public ReadingsPresenter(DapController dapController) {
		this.dapController = dapController;
		series = new ArrayList<>();
		displayedDevices = new HashMap<>();
		additionalParameters = new HashMap<>();
		chosenAdditionalReadings = new ArrayList<>();
		additionalDevices = new HashMap<>();
	}

	public void onShowExpandedReadings(Levee levee, List<ChartSeries> series) {
		this.levee = levee;
		this.series.clear();
		this.series.addAll(series);
		view.showModal(true);
	}

	public void onDeviceSeriesHover(String deviceId, boolean hover) {
		if(displayedDevices.containsKey(deviceId)) {
			if (hover) {
				mapPresenter.select(displayedDevices.get(deviceId));
			} else {
				mapPresenter.unselect(displayedDevices.get(deviceId));
			}
		}
	}

	@Override
	public void onModalShown() {
		if(mapPresenter == null) {
			mapPresenter = eventBus.addHandler(MapPresenter.class);
			view.setMap(mapPresenter.getView());
		}

		mapPresenter.reset(false);
		dapController.getSections(levee.getId(), new SectionsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}

			@Override
			public void processSections(List<Section> sections) {
				for(Section section : sections) {
					mapPresenter.add(section);
				}
			}
		});

		if(chartPresenter == null) {
			chartPresenter = eventBus.addHandler(ChartPresenter.class);
			chartPresenter.setHeight(view.getChartContainerHeight());
			chartPresenter.setDeviceSelectHandler(new ChartPresenter.DeviceSelectHandler() {
				@Override
				public void select(ChartSeries series) {
					eventBus.deviceSeriesHover(series.getDeviceId(), true);
				}

				@Override
				public void unselect(ChartSeries series) {
					eventBus.deviceSeriesHover(series.getDeviceId(), false);
				}
			});
			chartPresenter.setZoomDataCallback(new TimelineZoomDataCallbackHelper(dapController,
					eventBus, chartPresenter));
			view.setChart(chartPresenter.getView());
		}

		chartPresenter.reset();

		List<String> deviceIds = new ArrayList<>();

		for(ChartSeries chartSeries : series) {
			chartPresenter.addChartSeries(chartSeries);
			deviceIds.add(chartSeries.getDeviceId());
		}

		drawDevices(deviceIds);
		completeAdditionalReadings();
	}

	@Override
	public void onAdditionalReadingsPicked(String parameterId) {
		if(additionalParameters.containsKey(parameterId)
				&& !chosenAdditionalReadings.contains(parameterId)) {
			if(chosenAdditionalReadings.size() == 0) {
				view.showNoAdditionalReadingsLabel(false);
			}

			Parameter parameter = additionalParameters.get(parameterId);
			view.addAdditionalReadingsLabel(parameterId,
					additionalDevices.get(parameter.getDeviceId()).getCustomId()
					+ " - " + parameter.getParameterName());
			view.setSelectedAdditionalReadings(PICK_VALUE);
			chosenAdditionalReadings.add(parameterId);
			addAdditionalReadings(parameterId);
		}
	}

	@Override
	public void onAdditionalReadingsRemoved(String parameterId) {
		if(chosenAdditionalReadings.contains(parameterId)) {
			view.removeAdditionalReadingsLabel(parameterId);
			chosenAdditionalReadings.remove(parameterId);

			if(chosenAdditionalReadings.size() == 0) {
				view.showNoAdditionalReadingsLabel(true);
			}

			removeAdditionalReadings(parameterId);
		}
	}

	private void addAdditionalReadings(final String parameterId) {
		chartPresenter.setLoadingState(true);
		dapController.getContext("measurements", new ContextsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
				chartPresenter.setLoadingState(false);
			}

			@Override
			public void processContexts(List<Context> contexts) {
				if(contexts.size() > 0) {
					dapController.getTimeline(contexts.get(0).getId(), parameterId,
							new TimelinesCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							eventBus.showError(errorDetails);
							chartPresenter.setLoadingState(false);
						}

						@Override
						public void processTimelines(List<Timeline> timelines) {
							if(timelines.size() > 0) {
								final Timeline timeline = timelines.get(0);
								dapController.getMeasurementsWithQuantity(timeline.getId(), 1000,
										new MeasurementsCallback() {
									@Override
									public void onError(ErrorDetails errorDetails) {
										eventBus.showError(errorDetails);
										chartPresenter.setLoadingState(false);
									}

									@Override
									public void processMeasurements(
											List<Measurement> measurements) {
										chartPresenter.setLoadingState(false);

										if(measurements.size() > 0) {
											ChartSeries chartSeries = new ChartSeries();
											String  additionalDeviceId = additionalParameters.get(
													parameterId).getDeviceId();
											chartSeries.setName(additionalDevices
													.get(additionalDeviceId).getCustomId() +
													" (" + additionalParameters.get(parameterId)
													.getMeasurementTypeName() + ")");
											chartSeries.setDeviceId(additionalDeviceId);
											chartSeries.setParameterId(parameterId);
											chartSeries.setLabel(additionalParameters
													.get(parameterId).getMeasurementTypeName());
											chartSeries.setUnit(additionalParameters
													.get(parameterId).getMeasurementTypeUnit());
											chartSeries.setTimelineId(timeline.getId());

											if (additionalDevices.get(additionalDeviceId)
													.getCustomId().contains("Wysokość wody")) {
												chartSeries.setOverrideColor("#278cdc");
												chartSeries.setOverrideLineWidth(4);
											}

											Number[][] values = new Number[measurements.size()][2];
											int index = 0;

											for(Measurement measurement : measurements) {
												values[index][0] = measurement.getTimestamp()
														.getTime();
												values[index][1] = measurement.getValue();
												index++;
											}

											chartSeries.setValues(values);
											chartPresenter.addChartSeries(chartSeries);
										}
									}
								});
							}
						}
					});
				}
			}
		});
	}

	private void removeAdditionalReadings(String parameterId) {
		chartPresenter.removeChartSeriesForParameter(additionalParameters.get(parameterId));
	}

	private void completeAdditionalReadings() {
		view.resetAdditionalReadings();
		view.showNoAdditionalReadingsLabel(true);
		view.addAdditionalReadingsOption(PICK_VALUE, view.pickAdditionalReadingLabel());
		additionalDevices.clear();
		additionalParameters.clear();
		chosenAdditionalReadings.clear();
		dapController.getDevicesForType(Arrays.asList("weather_station", "pump", "external_data_source"), new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}

			@Override
			public void processDevices(List<Device> devices) {
				List<String> deviceIds = new ArrayList<>();

				for(Device device : devices) {
					deviceIds.add(device.getId());
					additionalDevices.put(device.getId(), device);
				}

				dapController.getParameters(deviceIds, new ParametersCallback() {
					@Override
					public void onError(ErrorDetails errorDetails) {
						eventBus.showError(errorDetails);
					}

					@Override
					public void processParameters(List<Parameter> parameters) {
						additionalParameters.clear();

						for(Parameter parameter : parameters) {
							view.addAdditionalReadingsOption(parameter.getId(),
									additionalDevices.get(parameter.getDeviceId()).getCustomId()
									+ " - " + parameter.getParameterName());
							additionalParameters.put(parameter.getId(), parameter);
						}
					}
				});
			}
		});
	}

	private void drawDevices(List<String> deviceIds) {
		dapController.getDevices(deviceIds, new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}

			@Override
			public void processDevices(List<Device> devices) {
				displayedDevices.clear();

				for(Device device : devices) {
					mapPresenter.add(device);
					displayedDevices.put(device.getId(), device);
				}
			}
		});
	}
}
