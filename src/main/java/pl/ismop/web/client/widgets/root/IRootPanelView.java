package pl.ismop.web.client.widgets.root;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRootPanelView extends IsWidget {
	interface IRootPresenter {
		void onShowSensors(boolean show);
		
		void onShowExperiments();
		
		void onShowExperiment();

		void onShowWeatherStation();

		void onShowFibreData();
	}

	void setSidePanel(IsWidget view);
}