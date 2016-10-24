package pl.ismop.web.client.widgets.analysis.chart;

import org.moxieapps.gwt.highcharts.client.PlotLine;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ChartView extends Composite implements IChartView {
    private static ChartViewUiBinder uiBinder = GWT.create(ChartViewUiBinder.class);
    interface ChartViewUiBinder extends UiBinder<Widget, ChartView> {}

    @UiField
    FlowPanel chartPanel;

    @UiField
    ChartMessages messages;

    private PlotLine currentTimePlotLine;

    public ChartView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setChart(pl.ismop.web.client.widgets.common.chart.IChartView view) {
        chartPanel.add(view);
    }

    @Override
    public ChartMessages getMessages() {
    	return messages;
    }
}
