package pl.ismop.web.client.widgets.root;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRootPanelView extends IsWidget {
	interface IRootPresenter {
		void onMonitoringViewOption();

		void onAnalysisViewOption();

		void onBrokenDevicesClicked();

		void onRealTimeViewOption();
	}

	void markAnalysisOption(boolean mark);

	void markMonitoringOption(boolean mark);

	void clearPanels();

	void setSidePanelWidget(IsWidget view);

	void setMainPanelWidget(IsWidget view);

	void setBrokenDevicesLinkLabel(int numberOfBrokenDevices);

	void showBrokenDevicesLink(boolean show, boolean alert);

	void showDetails(List<String> brokenParameters);

	void markRealTimeOption(boolean mark);
}