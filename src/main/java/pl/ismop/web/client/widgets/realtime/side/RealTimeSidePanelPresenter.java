package pl.ismop.web.client.widgets.realtime.side;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.realtime.side.IRealTimeSidePanelView.IRealTimeSidePanelPresenter;

@Presenter(view = RealTimeSidePanelView.class, multiple = true)
public class RealTimeSidePanelPresenter extends BasePresenter<IRealTimeSidePanelView, MainEventBus> implements IRealTimeSidePanelPresenter {
	private static final int REFRESH_INTERVAL_SECONDS = 60;
	
	private static final int TIME_TICK_MILLIS = 1000;
	
	private int countdown;
	
	public void init() {
		countdown = REFRESH_INTERVAL_SECONDS;
		view.startTimer(TIME_TICK_MILLIS);
	}
	
	public void onRealDataContentLoaded() {
		init();
	}

	@Override
	public void onRefreshRequested() {
		eventBus.refreshRealTimePanel();
		countdown = 0;
		view.setProgress(Double.valueOf(((double) countdown / REFRESH_INTERVAL_SECONDS) * 100).intValue());
		view.stopTimer();
	}

	@Override
	public void onTimeTick() {
		if (countdown <= 0) {
			eventBus.refreshRealTimePanel();
			view.stopTimer();
		}
		
		view.setProgress(Double.valueOf(((double) countdown / REFRESH_INTERVAL_SECONDS) * 100).intValue());
		countdown--;
	}
}