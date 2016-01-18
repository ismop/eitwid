package pl.ismop.web.client.widgets.realtime.side;

import org.gwtbootstrap3.client.ui.ProgressBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.realtime.side.IRealTimeSidePanelView.IRealTimeSidePanelPresenter;

public class RealTimeSidePanelView extends Composite implements IRealTimeSidePanelView , ReverseViewInterface<IRealTimeSidePanelPresenter> {
	private static RealTimeSidePanelViewUiBinder uiBinder = GWT.create(RealTimeSidePanelViewUiBinder.class);

	interface RealTimeSidePanelViewUiBinder extends UiBinder<Widget, RealTimeSidePanelView> {}
	
	private IRealTimeSidePanelPresenter presenter;
	
	private Timer timer;
	
	@UiField
	ProgressBar progressBar;

	public RealTimeSidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("progressFocus")
	void onProgressClicked(ClickEvent event) {
		getPresenter().onRefreshRequested();
	}

	@Override
	public void setPresenter(IRealTimeSidePanelPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IRealTimeSidePanelPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void startTimer(int tickMillis) {
		timer = new Timer() {
			@Override
			public void run() {
				getPresenter().onTimeTick();
			}
		};
		timer.scheduleRepeating(tickMillis);
	}

	@Override
	public void setProgress(int progress) {
		progressBar.setPercent(progress);
	}

	@Override
	public void stopTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}
}