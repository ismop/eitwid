package pl.ismop.web.client.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;

public class ChartSeriesUtil {
	public static Stream<ChartSeries> toChartSeries(List<Device> devices, List<Parameter> parameters, List<Timeline> timelines, List<Measurement> measurements) {
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

	private static ChartSeries createSeries(Device device, Parameter parameter, Timeline timeline, List<Measurement> measurements) {
		ChartSeries chartSeries = new ChartSeries();
		chartSeries.setName(device.getCustomId() + " (" + parameter.getMeasurementTypeName() + ")");
		chartSeries.setDeviceId(device.getId());
		chartSeries.setParameterId(parameter.getId());
		chartSeries.setLabel(parameter.getMeasurementTypeName());
		chartSeries.setUnit(parameter.getMeasurementTypeUnit());
		chartSeries.setTimelineId(timeline.getId());

        if (device.getCustomId().contains("Wysokość wody")) {
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

		return chartSeries;
	}
}
