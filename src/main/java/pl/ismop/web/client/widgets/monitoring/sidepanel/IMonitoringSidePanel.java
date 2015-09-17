package pl.ismop.web.client.widgets.monitoring.sidepanel;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.ui.IsWidget;

public interface IMonitoringSidePanel extends IsWidget {
	interface IMonitoringSidePanelPresenter {
		void handleShowFibreClick();

		void handleShowWeatherClick();
	}

	void showLeveeName(boolean show);

	void showLeveeList(boolean show);

	void showLeveeProgress(boolean show);

	void showNoLeveesMessage();

	void addLeveeOption(String leveeid, String leveeName);

	void setLeveeName(String leveeName);

	String getNameLabel();

	String getInternalIdLabel();

	void addMetadata(String label, String value);

	void clearMetadata();

	void setChart(Chart chart);

	Number getChartContainerHeight();

	void showChartExpandButton(boolean show);
}