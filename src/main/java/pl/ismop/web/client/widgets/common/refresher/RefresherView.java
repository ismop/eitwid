package pl.ismop.web.client.widgets.common.refresher;

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

public class RefresherView extends Composite
		implements IRefresherView, ReverseViewInterface<IRefresherView.IRefresherPresenter> {
	private static SliderViewUiBinder uiBinder = GWT.create(SliderViewUiBinder.class);

	interface SliderViewUiBinder extends UiBinder<Widget, RefresherView> {
	}

	private IRefresherPresenter presenter;

	private Timer timer;

	@UiField
	ProgressBar progressBar;
	
	public RefresherView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("progressFocus")
	void onProgressClicked(ClickEvent event) {
		getPresenter().onRefreshRequested();
	}
	
	@Override
	public void startTimer(int tickMillis) {
		stopTimer();
		
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
		
		setProgress(0);
	}

	@Override
	public void setPresenter(IRefresherPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IRefresherPresenter getPresenter() {
		return presenter;
	}

}