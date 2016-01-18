package pl.ismop.web.client.widgets.realtime.side;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;

@Presenter(view = RealTimeSidePanelView.class, multiple = true)
public class RealTimeSidePanelPresenter extends BasePresenter<IRealTimeSidePanelView, MainEventBus> {

	public void init() {
		// TODO Auto-generated method stub
		
	}
}