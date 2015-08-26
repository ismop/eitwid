package pl.ismop.web.client.widgets.weather;

import org.moxieapps.gwt.highcharts.client.StockChart;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;

public interface IWeatherStationView extends IsWidget {
	interface IWeatherStationPresenter {
		
	}

	void showModal();

	void showProgress1(boolean show);
	
	void showProgress2(boolean show);

	HasText getSecondHeading();

	HasText getFirstHeading();

	void setFirstChart(StockChart firstChart);

	HasVisibility getFirstNoDataMessage();

	HasVisibility getFirstProgress();
	
	HasVisibility getSecondNoDataMessage();

	HasVisibility getSecondProgress();

	HasVisibility getSecondChartVisibility();

	HasVisibility getFirstChartVisibility();
}