package pl.ismop.web.client.widgets.common.chart;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.ui.IsWidget;

public interface IChartView extends IsWidget {
	interface IChartPresenter {
	}

	void addChart(Chart chart);

	String getChartTitle();

	String getLoadingMessage();

	String getDownloadCSVMessage();

	void showLoadingMessage(boolean show);
}
