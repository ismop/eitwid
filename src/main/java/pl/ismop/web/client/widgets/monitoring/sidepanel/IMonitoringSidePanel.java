package pl.ismop.web.client.widgets.monitoring.sidepanel;

import com.google.gwt.user.client.ui.IsWidget;

public interface IMonitoringSidePanel extends IsWidget {
	interface IMonitoringSidePanelPresenter {
		void handleShowFibreClick();

		void handleShowWeatherClick();

		void onExpandChart();
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

	void showNoMeasurementsForDeviceMessage();

	void setChart(IsWidget view);

	int getChartHeight();

	void showChartExpandButton(boolean show);

	String getProfileTypeLabel();

	String getTypeLabel();

	String getSectionTypeLabel();

	String getDeviceTypeLabel();

	String getDeviceAggregateTypeLabel();
}