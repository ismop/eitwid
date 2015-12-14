package pl.ismop.web.client.widgets.monitoring.weather;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;

public interface IWeatherStationView extends IsWidget {
	
	interface IWeatherStationPresenter {
		public void onModalShown();
				
		public void onModalHidden();
		
		public void loadParameter(String parameterId, Boolean value);
	}

	void showModal();

	void showProgress(boolean show);

	void setChart(Chart firstChart);

	HasVisibility getChartVisibility();

	HasText getHeading1();

	HasText getHeading2();

	HasVisibility getContentVisibility();

	void clearMeasurements();
	
	void addLatestReading1(String parameterId, String parameterName, String typeName, String value, String unit, String timestamp);

	void addLatestReading2(String parameterId, String parameterName, String typeName, String value, String unit, String timestamp);
	
	public int getChartContainerHeight();

	void setChart(IsWidget chart);

	String getNoReadingLabel();
}