package pl.ismop.web.client.widgets.monitoring.readings;

import com.google.gwt.user.client.ui.IsWidget;

public interface IReadingsView extends IsWidget {
	interface IReadingsPresenter {
		void onModalShown();

		void onAdditionalReadingsPicked(String value);

		void onAdditionalReadingsRemoved(String parameterId);
	}

	void showModal(boolean show);

	void setMap(IsWidget map);

	void setChart(IsWidget chart);

	int getChartContainerHeight();

	void addAdditionalReadingsOption(String id, String parameterName);

	void resetAdditionalReadings();

	String pickAdditionalReadingLabel();

	void setSelectedAdditionalReadings(String optionId);

	void addAdditionalReadingsLabel(String id, String label);

	void showNoAdditionalReadingsLabel(boolean show);

	void removeAdditionalReadingsLabel(String id);
}