package pl.ismop.web.client.widgets.root;

import java.util.ArrayList;
import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = RootPanel.class)
public class RootPresenter extends BasePresenter<IRootPanelView, MainEventBus> implements IRootPresenter{
	private List<LeveeSummaryPresenter> leveePresenters;

	@Inject
	public RootPresenter() {
		leveePresenters = new ArrayList<>();
	}
	
	public void onStart() {
		RootLayoutPanel.get().add(view);
		eventBus.drawGoogleMap("mapPanel");
	}
	
	public void onShowExperiments(List<String> experimentIds) {
		for(LeveeSummaryPresenter presenter : leveePresenters) {
			presenter.stopUpdate();
		}
	}

	@Override
	public void onShowSensors(boolean show) {
		eventBus.showSensors(show);
	}

	@Override
	public void onShowLevees(boolean show) {
		eventBus.showLevees(show);
	}
}