package pl.ismop.web.client.widgets.analysis.chart;

/**
 * Created by marek on 07.10.15.
 */
public interface IChartView {
    void setChart(pl.ismop.web.client.widgets.common.chart.IChartView view);
    ChartMessages getMessages();
}