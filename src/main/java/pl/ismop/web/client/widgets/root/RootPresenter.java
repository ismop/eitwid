package pl.ismop.web.client.widgets.root;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;

@Presenter(view = RootPanel.class)
public class RootPresenter extends BasePresenter<IRootPanelView, MainEventBus> implements IRootPresenter{
	private List<String> experimentIds;
	
	public void onMonitoringPanel() {
		view.markAnalysisOption(false);
		view.markMonitoringOption(true);
	}
	
	public void onAnalysisPanel() {
		view.markAnalysisOption(true);
		view.markMonitoringOption(false);
	}
	
	public void onExperimentCreated(Experiment experiment) {
		experimentIds.add(experiment.getId());
	}
	
	public void onSetSidePanel(IsWidget view) {
		this.view.setSidePanel(view);
	}

	@Override
	public void onMonitoringViewOption() {
		eventBus.monitoringPanel();
	}

	@Override
	public void onAnalysisViewOption() {
		eventBus.analysisPanel();
	}
}