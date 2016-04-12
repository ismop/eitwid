package pl.ismop.web.client.widgets.realtime.main;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRealTimePanelView extends IsWidget {
	interface IRealTimePanelPresenter {
		void onWeatherSourceChange();
	}

	void setWeatherSectionTitle(String weatherDeviceName);

	void setWeatherParameter(int index, String name, String value, String date);

	void setChartView(IsWidget view);
}