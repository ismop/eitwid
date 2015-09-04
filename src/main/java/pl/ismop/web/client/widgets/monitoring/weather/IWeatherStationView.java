package pl.ismop.web.client.widgets.monitoring.weather;

import org.moxieapps.gwt.highcharts.client.StockChart;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;

public interface IWeatherStationView extends IsWidget {
	interface IWeatherStationPresenter {
		
	}

	void showModal();

	void showProgress(boolean show);

	void setChart(StockChart firstChart);

	HasVisibility getChartVisibility();

	HasText getHeading1();

	HasText getHeading2();

	HasVisibility getContentVisibility();

	void addLatestReading1(String label, String value, String unit, String timestamp);

	void clearMeasurements();

	void addLatestReading2(String label, String value, String unit, String timestamp);
}