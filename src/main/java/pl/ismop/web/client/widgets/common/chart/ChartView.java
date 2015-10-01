package pl.ismop.web.client.widgets.common.chart;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class ChartView extends Composite implements IChartView {
	private static ChartViewUiBinder uiBinder = GWT.create(ChartViewUiBinder.class);

	interface ChartViewUiBinder extends UiBinder<Widget, ChartView> {}

	@UiField
	ChartMessages messages;
	
	@UiField
	HTMLPanel panel;
	
	@UiField
	FlowPanel loadingPanel;
	
	public ChartView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void addChart(Chart chart) {
		panel.add(chart);
	}

	@Override
	public String getChartTitle() {
		return messages.chartTitle();
	}

	@Override
	public String getLoadingMessage() {
		return messages.loadingMessage();
	}

	@Override
	public void showLoadingMessage(boolean show) {
		loadingPanel.setVisible(show);
	}
}