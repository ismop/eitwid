package pl.ismop.web.client.widgets.monitoring.readings;

import com.google.gwt.user.client.ui.IsWidget;

public interface IReadingsView extends IsWidget {
	interface IReadingsPresenter {
		void onModalShown();
	}

	void showModal(boolean show);

	void setMap(IsWidget map);

	void setChart(IsWidget chart);
}