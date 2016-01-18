package pl.ismop.web.client.widgets.realtime.side;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRealTimeSidePanelView extends IsWidget {
	interface IRealTimeSidePanelPresenter {
		void onRefreshRequested();

		void onTimeTick();
	}

	void startTimer(int tickMillis);

	void setProgress(int progress);

	void stopTimer();
}