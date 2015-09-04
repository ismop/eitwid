package pl.ismop.web.client.widgets.root;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRootPanelView extends IsWidget {
	interface IRootPresenter {
		void onMonitoringViewOption();

		void onAnalysisViewOption();
	}

	void setSidePanel(IsWidget view);

	void markAnalysisOption(boolean mark);

	void markMonitoringOption(boolean mark);
}