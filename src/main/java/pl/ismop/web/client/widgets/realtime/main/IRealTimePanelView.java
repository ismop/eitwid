package pl.ismop.web.client.widgets.realtime.main;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRealTimePanelView extends IsWidget {
	interface IRealTimePanelPresenter {
		void onWeatherSourceChange();

		void onVerticalSliceParameterChange();
	}

	void setWeatherSectionTitle(String weatherDeviceName);

	void setWeatherParameter(int index, String name, String value, String date);

	void setChartView(IsWidget view);

	void showLoadingIndicator(boolean show);

	void setEmptyWaterLevelValues();

	void setWaterLevelValue(String value);

	void setWaterLevelDate(String formatForDisplay);

	void setVerticalSliceView(IsWidget view);

	void setVerticalSliceHeading(String parameterName);

	void setHorizontalSliceView(IsWidget view);
}