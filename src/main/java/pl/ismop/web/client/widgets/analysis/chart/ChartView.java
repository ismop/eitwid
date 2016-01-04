package pl.ismop.web.client.widgets.analysis.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.moxieapps.gwt.highcharts.client.*;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOverEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.common.DateChartPoint;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartView extends Composite implements IChartView, ReverseViewInterface<IChartView.IChartPresenter> {
    private static ChartViewUiBinder uiBinder = GWT.create(ChartViewUiBinder.class);
    interface ChartViewUiBinder extends UiBinder<Widget, ChartView> {}

    @UiField
    ChartMessages messages;

    @UiField
    FlowPanel chartPanel;

    Chart chart;

    private PlotLine currentTimePlotLine;
    private IChartPresenter presenter;
    private Map<String, Timeline> nameToTimeline = new HashMap<>();
    private Map<String, Number> parameterToYChar = new HashMap<>();

    public ChartView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public ChartMessages getMessages() {
        return messages;
    }

    @Override
    public void showLoading(String message) {
        initChart();
        chart.showLoading(message);
    }

    @Override
    public void setSeries(Map<Timeline, List<DateChartPoint>> timelineToMeasurements) {
        chart.removeAllSeries();
        chart.hideLoading();
        nameToTimeline.clear();

        boolean dataExists = false;
        for (Map.Entry<Timeline, List<DateChartPoint>> timelineListEntry : timelineToMeasurements.entrySet()) {
            Parameter parameter = timelineListEntry.getKey().getParameter();
            String name = parameter.getDevice().getCustomId() + " (" + parameter.getMeasurementTypeName() + ") " +
                    timelineListEntry.getKey().getLabel();

            Series series = chart.createSeries().
                    setName(name).
                    setType(Series.Type.SPLINE).
                    setYAxis(getYAxisIndex(parameter));

            nameToTimeline.put(name, timelineListEntry.getKey());
            for (DateChartPoint point : timelineListEntry.getValue()) {
                series.addPoint(point.getX().getTime(), point.getY());
                dataExists = true;
            }
            chart.addSeries(series);
        }
        if (!dataExists) {
            chart.showLoading(messages.noMeasurements());
        }
    }

    private Number getYAxisIndex(Parameter parameter) {
        String yAxisLabel = parameter.getMeasurementTypeName() + ", [" + parameter.getMeasurementTypeUnit() + "]";

        if(parameterToYChar.containsKey(yAxisLabel)) {
            return parameterToYChar.get(yAxisLabel);
        } else {
            int index = parameterToYChar.size();

            if(index == 0) {
                updateFirstYAxis(chart.getNativeChart(), yAxisLabel);
            } else {
                addAxis(chart.getNativeChart(), index, yAxisLabel);
            }

            parameterToYChar.put(yAxisLabel, index);

            return index;
        }
    }

    private native void updateFirstYAxis(JavaScriptObject nativeChart, String yAxisLabel) /*-{
        nativeChart.yAxis[0].update({
            showEmpty: false,
            title: {
                text: yAxisLabel
            },
            labels: {
                format: "{value:.2f}"
            }
        });
    }-*/;

    private native void addAxis(JavaScriptObject nativeChart, int index, String yAxisLabel) /*-{
        nativeChart.addAxis({
            showEmpty: false,
            title: {
                text: yAxisLabel
            },
            labels: {
                format: "{value:.2f}"
            }
        });
    }-*/;

    private void initChart() {
        if (chart == null) {
            chart = new Chart().setChartTitle(new ChartTitle().setText(null));
            chart.getXAxis()
                    .setType(Axis.Type.DATE_TIME)
                    .setDateTimeLabelFormats(new DateTimeLabelFormats()
                                    .setMonth("%e. %b")
                                    .setYear("%b")
                    );
            chart.getYAxis().setAxisTitleText(null);
            chart.setOption("/chart/zoomType", "x");

            chart.setSeriesPlotOptions(new SeriesPlotOptions().
                    setSeriesMouseOverEventHandler(new SeriesMouseOverEventHandler() {
                        @Override
                        public boolean onMouseOver(SeriesMouseOverEvent seriesMouseOverEvent) {
                            getPresenter().timelineSelected(nameToTimeline.get(seriesMouseOverEvent.getSeriesName()));
                            return true;
                        }
                    }));

            chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
                private NumberFormat formatter = NumberFormat.getFormat("00.00");

                @Override
                public String format(ToolTipData toolTipData) {
                    String msg = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT).
                            format(new Date(toolTipData.getXAsLong())) + "<br/>";

                    for(Point point : toolTipData.getPoints()) {
                        JavaScriptObject nativePoint = point.getNativePoint();
                        String seriesName = getSeriesName(nativePoint);
                        Timeline timeline = nameToTimeline.get(seriesName);

                        if (timeline != null) {
                            Parameter parameter = timeline.getParameter();
                            msg += "<br/><span style=\"color:" + getPointColor(nativePoint) +
                                    "\">\u25CF</span> " + seriesName + ": <b>" +
                                    NumberFormat.getFormat("0.00").format(point.getY()) + " " +
                                    parameter.getMeasurementTypeUnit() + "</b><br/>";
                        }
                    }

                    return msg;
                }

                private native String getSeriesId(JavaScriptObject point) /*-{
                    return point.series.options.id;
                }-*/;

                private native String getSeriesName(JavaScriptObject point) /*-{
                    return point.series.name;
                }-*/;

                private native String getPointColor(JavaScriptObject nativePoint) /*-{
                    return nativePoint.color;
                }-*/;
            }).setShared(true));
            chartPanel.add(chart);
        }
    }

    @Override
    public void selectDate(Date selectedDate, String color) {
        initChart();

        if (currentTimePlotLine != null) {
            chart.getXAxis().removePlotLine(currentTimePlotLine);
        }
        currentTimePlotLine = chart.getXAxis().createPlotLine().
                setWidth(2).setColor(color).setValue(selectedDate.getTime());
        chart.getXAxis().addPlotLines(currentTimePlotLine);
    }

    @Override
    public void setInterval(Date startDate, Date endDate) {
        chart.getXAxis().setMin(startDate.getTime());
        chart.getXAxis().setMax(endDate.getTime());
    }

    @Override
    public void setPresenter(IChartPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public IChartPresenter getPresenter() {
        return presenter;
    }
}
