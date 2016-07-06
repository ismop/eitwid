package pl.ismop.web.client.widgets.monitoring.waterheight;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.gwt.core.shared.GWT;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.util.TimelineZoomDataCallbackHelper;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.delegator.ContextsCallback;
import pl.ismop.web.client.widgets.delegator.DevicesCallback;
import pl.ismop.web.client.widgets.delegator.MeasurementsCallback;
import pl.ismop.web.client.widgets.delegator.ParametersCallback;
import pl.ismop.web.client.widgets.delegator.TimelinesCallback;
import pl.ismop.web.client.widgets.monitoring.waterheight.IWaterHeightView.IWaterHeightPresenter;

@Presenter(view = WaterHeightView.class)
public class WaterHeightPresenter extends BasePresenter<IWaterHeightView, MainEventBus>
		implements IWaterHeightPresenter, ErrorCallback {

	private DapController dapController;
	private ChartPresenter waterHeightChart;

	// use Level2_PV sensor

	@Inject
	public WaterHeightPresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void onShowWaterHightPanel(Levee selectedLevee) {
		view.showModal(true);
	}

	@Override
	public void onModalReady() {
		initChart();
		loadWaterHeight();
	}

	private void initChart() {
		if (waterHeightChart != null) {

		} else {
			waterHeightChart = eventBus.addHandler(ChartPresenter.class);
			waterHeightChart.setHeight(view.getChartHeight());
			view.setChart(waterHeightChart.getView());
			waterHeightChart.initChart();
			waterHeightChart
					.setZoomDataCallback(new TimelineZoomDataCallbackHelper(dapController, eventBus, waterHeightChart));
		}
	}

	private void loadWaterHeight() {
		waterHeightChart.setLoadingState(true);
		
		GWT.log("Loading measurements context");
		dapController.getContext("measurements", new ContextsCallback(this) {
			@Override
			public void processContexts(List<Context> contexts) {
				final Context context = contexts.get(0);
				GWT.log("Loading pump devices");
				dapController.getDevicesForType("pump", new DevicesCallback(this) {
					@Override
					public void processDevices(final List<Device> devices) {
						GWT.log("Loading pump devies parameters");
						dapController.getParameters(Lists.transform(devices, Device::getId), new ParametersCallback(this) {
							@Override
							public void processParameters(List<Parameter> parameters) {
								List<Parameter> filteredParameters = parameters.stream().filter(p -> p.getCustomId().contains("PV")).collect(Collectors.toList());
								GWT.log("Loading pump devies parameters timelines");
								dapController.getTimelinesForParameterIds(context.getId(), Lists.transform(filteredParameters, Parameter::getId), new TimelinesCallback(this) {
									@Override
									public void processTimelines(List<Timeline> timelines) {
										GWT.log("Loading pump devies parameters timelines measurements");
										dapController.getMeasurementsForTimelineIdsWithQuantity(Lists.transform(timelines, Timeline::getId), 1000, new MeasurementsCallback(this) {
											@Override
											public void processMeasurements(List<Measurement> measurements) {
												GWT.log("Generating map series");
												mapToChartSeries(devices, filteredParameters, timelines, measurements).
													forEach(chartSeries -> waterHeightChart.addChartSeries(chartSeries));
												waterHeightChart.setLoadingState(false);
											}
										});
									}
								});
							}
						});
					}
				});
			}
		});
	}
	
	private Stream<ChartSeries> mapToChartSeries(List<Device> devices, List<Parameter> parameters, List<Timeline> timelines, List<Measurement> measurements) {
		Map<String, Device> idToDevice = devices.stream().collect(Collectors.toMap(d -> d.getId(), Function.identity()));
		Map<String, Parameter> idToParameter = parameters.stream().collect(Collectors.toMap(p -> p.getId(), Function.identity()));
		Map<String, Timeline> idToTimeline = timelines.stream().collect(Collectors.toMap(t -> t.getId(), Function.identity()));
		
		return measurements.stream().collect(Collectors.groupingBy(m -> m.getTimelineId())).
				entrySet().stream().map(entry -> {
					Timeline timeline = idToTimeline.get(entry.getKey());
					Parameter parameter = idToParameter.get(timeline.getParameterId());
					Device device = idToDevice.get(parameter.getDeviceId());
					
					return createSeries(device, parameter, timeline, entry.getValue());
				});
	}
	
	private ChartSeries createSeries(Device device, Parameter parameter, Timeline timeline, List<Measurement> measurements) {
		ChartSeries chartSeries = new ChartSeries();
		chartSeries.setName(device.getCustomId() + " (" + parameter.getMeasurementTypeName() + ")");
		chartSeries.setDeviceId(device.getId());
		chartSeries.setParameterId(parameter.getId());
		chartSeries.setLabel(parameter.getMeasurementTypeName());
		chartSeries.setUnit(parameter.getMeasurementTypeUnit());
		chartSeries.setTimelineId(timeline.getId());
		
		Number[][] values = new Number[measurements.size()][2];
		int index = 0;
		
		for(Measurement measurement : measurements) {
			values[index][0] = measurement.getTimestamp()
					.getTime();
			values[index][1] = measurement.getValue();
			index++;
		}
		chartSeries.setValues(values);
		
		return chartSeries;
	}
	
	@Override
	public void onError(ErrorDetails errorDetails) {
		waterHeightChart.setLoadingState(false);
		eventBus.showError(errorDetails);
	}
}
