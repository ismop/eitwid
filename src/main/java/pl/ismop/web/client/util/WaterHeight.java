package pl.ismop.web.client.util;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;

import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.delegator.ContextsCallback;
import pl.ismop.web.client.widgets.delegator.DevicesCallback;
import pl.ismop.web.client.widgets.delegator.MeasurementsCallback;
import pl.ismop.web.client.widgets.delegator.ParametersCallback;
import pl.ismop.web.client.widgets.delegator.TimelinesCallback;

public class WaterHeight {
	public interface WaterHeightCallback extends ErrorCallback {
		void success(Stream<ChartSeries> series);
	}

	private abstract class MeasurementLoader implements ErrorCallback {
		private ErrorCallback errorCallback;

		public MeasurementLoader(ErrorCallback errorCallback) {
			this.errorCallback = errorCallback;
		}

		@Override
		public void onError(ErrorDetails errorDetails) {
			errorCallback.onError(errorDetails);
		}

		public abstract void run(List<Device> devices, List<Parameter> parameters, List<Timeline> timelines);
	}

	private DapController dapController;

	public WaterHeight(DapController dapController) {
		this.dapController = dapController;
	}


	public void load(final WaterHeightCallback callback) {
		loadDeviceStructure("PV", new MeasurementLoader(callback) {
			@Override
			public void run(List<Device> devices, List<Parameter> parameters, List<Timeline> timelines) {
				dapController.getMeasurementsForTimelineIdsWithQuantity(Lists.transform(timelines, Timeline::getId), 1000, new MeasurementsCallback(callback) {
					@Override
					public void processMeasurements(List<Measurement> measurements) {
						callback.success(ChartSeriesUtil.toChartSeries(devices, parameters, timelines, measurements));
					}
				});
			}
		});
	}

	public void loadAverage(final WaterHeightCallback callback) {
		loadDeviceStructure("2_PV", new MeasurementLoader(callback) {
			@Override
			public void run(List<Device> devices, List<Parameter> parameters, List<Timeline> timelines) {
				dapController.getMeasurementsForTimelineIdsWithQuantity(Lists.transform(timelines, Timeline::getId), 1000, new MeasurementsCallback(callback) {
					@Override
					public void processMeasurements(List<Measurement> measurements) {
						callback.success(ChartSeriesUtil.toChartSeries(devices, parameters, timelines, measurements));
					}
				});
			}
		});
	}

	public void loadAverage(final Date from, final Date to, final WaterHeightCallback callback) {
		loadDeviceStructure("2_PV", new MeasurementLoader(callback) {
			@Override
			public void run(List<Device> devices, List<Parameter> parameters, List<Timeline> timelines) {
				dapController.getMeasurementsWithQuantityAndTime(Lists.transform(timelines, Timeline::getId), from, to, 1000, new MeasurementsCallback(callback) {
					@Override
					public void processMeasurements(List<Measurement> measurements) {
						callback.success(ChartSeriesUtil.toChartSeries(devices, parameters, timelines, measurements));
					}
				});
			}
		});
	}

	private void loadDeviceStructure(String parameterFilter, MeasurementLoader loader) {
		GWT.log("Loading measurements context");
		dapController.getContext("measurements", new ContextsCallback(loader) {
			@Override
			public void processContexts(List<Context> contexts) {
				final Context context = contexts.get(0);
				GWT.log("Loading pump devices");
				dapController.getDevicesForType("pump", new DevicesCallback(loader) {
					@Override
					public void processDevices(final List<Device> devices) {
						GWT.log("Loading pump devies parameters");
						dapController.getParameters(Lists.transform(devices, Device::getId), new ParametersCallback(loader) {
							@Override
							public void processParameters(List<Parameter> parameters) {
								List<Parameter> filteredParameters = parameters.stream().filter(p -> p.getCustomId().contains(parameterFilter)).collect(Collectors.toList());
								GWT.log("Loading pump devies parameters timelines");
								dapController.getTimelinesForParameterIds(context.getId(), Lists.transform(filteredParameters, Parameter::getId), new TimelinesCallback(loader) {
									@Override
									public void processTimelines(List<Timeline> timelines) {
										GWT.log("Loading pump devies parameters timelines measurements");
										loader.run(devices, filteredParameters, timelines);
									}
								});
							}
						});
					}
				});
			}
		});
	}
}
