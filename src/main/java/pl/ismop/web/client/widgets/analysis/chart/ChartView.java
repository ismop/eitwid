package pl.ismop.web.client.widgets.analysis.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.moxieapps.gwt.highcharts.client.*;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.common.DateChartPoint;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChartView extends Composite implements IChartView {
    private static ChartViewUiBinder uiBinder = GWT.create(ChartViewUiBinder.class);

    @UiField
    ChartMessages messages;

    @UiField
    Chart chart;
    private PlotLine currentTimePlotLine;

    interface ChartViewUiBinder extends UiBinder<Widget, ChartView> {}

    public ChartView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public ChartMessages getMessages() {
        return messages;
    }

    @Override
    public void showLoading(String message) {
        chart.showLoading(message);
    }

    @Override
    public void setSeries(Map<Timeline, List<DateChartPoint>> timelineToMeasurements) {
        chart.removeAllSeries();
        chart.getXAxis()
                .setType(Axis.Type.DATE_TIME)
                .setDateTimeLabelFormats(new DateTimeLabelFormats()
                                .setMonth("%e. %b")
                                .setYear("%b")
                );
        chart.hideLoading();

        boolean dataExists = false;
        for (Map.Entry<Timeline, List<DateChartPoint>> timelineListEntry : timelineToMeasurements.entrySet()) {
            Parameter parameter = timelineListEntry.getKey().getParameter();
            String name = parameter.getDevice().getCustomId() + " (" + parameter.getMeasurementTypeName() + ") " +
                    timelineListEntry.getKey().getLabel();

            Series series = chart.createSeries().setName(name).setType(Series.Type.SPLINE);
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

    @Override
    public void selectDate(Date selectedDate, String color) {
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
}
