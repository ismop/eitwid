package pl.ismop.web.client.widgets.common.refresher;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;

@Presenter(view = RefresherView.class, multiple = true)
public class RefresherPresenter extends BasePresenter<IRefresherView, MainEventBus>
		implements IRefresherView.IRefresherPresenter {

	public static interface Event {
		void refresh();
	}
	
	private static final int REFRESH_INTERVAL_SECONDS = 60;

	private static final int TIME_TICK_MILLIS = 1000;

	private int countdown;
	
	private Event event;	
	
	@Override
	public void onRefreshRequested() {
		view.setProgress(Double.valueOf(((double) countdown / REFRESH_INTERVAL_SECONDS) * 100).intValue());
		view.stopTimer();
		refresh();
	}

	@Override
	public void onTimeTick() {
		if (countdown <= 0) {			
			view.stopTimer();
			refresh();
		}

		view.setProgress(Double.valueOf(((double) countdown / REFRESH_INTERVAL_SECONDS) * 100).intValue());
		countdown--;
	}

	private void refresh() {
		if (event != null) {
			event.refresh();
		}
	}
	
	public void initializeTimer() {
		countdown = REFRESH_INTERVAL_SECONDS;
		view.startTimer(TIME_TICK_MILLIS);
	}

	public void disableTimers() {
		view.stopTimer();
	}
	
	public void setEvent(Event event) {
		this.event = event;
	}
}
