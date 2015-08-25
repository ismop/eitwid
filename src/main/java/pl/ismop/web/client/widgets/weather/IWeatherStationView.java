package pl.ismop.web.client.widgets.weather;

import org.moxieapps.gwt.highcharts.client.StockChart;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface IWeatherStationView extends IsWidget {
	interface IWeatherStationPresenter {
		
	}

	void showModal();

	void showProgress(boolean show);

	void showNoWeatherStationData(boolean show);

	void clearMessage();

	HasText getSecondHeading();

	HasText getFirstHeading();

	void showDataContainer(boolean show);

	void setFirstChart(StockChart firstChart);

	void setSecondChart(StockChart secondChart);
}