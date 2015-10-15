package pl.ismop.web.client.widgets.analysis.chart;

import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.common.DateChartPoint;

import java.util.List;
import java.util.Map;

/**
 * Created by marek on 07.10.15.
 */
public interface IChartView {
    ChartMessages getMessages();
    void showLoading(String message);
    void setSeries(Map<Timeline, List<DateChartPoint>> timelineToMeasurements);
}
