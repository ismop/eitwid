package pl.ismop.web.client.widgets.realtime.main;

import com.google.gwt.user.client.Timer;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;

@Presenter(view = RealTimePanelView.class, multiple = true)
public class RealTimePanelPresenter extends BasePresenter<IRealTimePanelView, MainEventBus> {

	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	public void onRefreshRealTimePanel() {
		new Timer() {
			@Override
			public void run() {
				//after content is refreshed notify the side panel that the counting can continue
				eventBus.realDataContentLoaded();
			}
		}.schedule(2000);
	}
}