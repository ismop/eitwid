package pl.ismop.web.client.widgets.common.refresher;


import com.google.gwt.user.client.ui.IsWidget;

import java.util.Date;

public interface IRefresherView extends IsWidget {
    interface IRefresherPresenter {

		void onRefreshRequested();

		void onTimeTick();
        
    }

	void startTimer(int tickMillis);

	void stopTimer();

	void setProgress(int progress);
}

