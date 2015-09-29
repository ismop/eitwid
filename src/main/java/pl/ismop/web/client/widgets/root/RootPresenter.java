package pl.ismop.web.client.widgets.root;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.comparison.ComparisonPresenter;
import pl.ismop.web.client.widgets.analysis.sidepanel.AnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.monitoring.mapnavigator.LeveeNavigatorPresenter;
import pl.ismop.web.client.widgets.monitoring.sidepanel.MonitoringSidePanelPresenter;
import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;

@Presenter(view = RootPanel.class)
public class RootPresenter extends BasePresenter<IRootPanelView, MainEventBus> implements IRootPresenter{
	private MonitoringSidePanelPresenter monitoringSidePanelPresenter;
	private LeveeNavigatorPresenter monitoringLeveeNavigator;
	private AnalysisSidePanelPresenter analysisPanelPresenter;
	private ComparisonPresenter comparisonPresenter;

	public void onMonitoringPanel() {
		view.markAnalysisOption(false);
		view.markMonitoringOption(true);
		view.clearPanels();
		
		if(monitoringSidePanelPresenter == null) {
			monitoringSidePanelPresenter = eventBus.addHandler(MonitoringSidePanelPresenter.class);
		}
		
		monitoringSidePanelPresenter.reset();
		view.setSidePanelWidget(monitoringSidePanelPresenter.getView());
		
		if(monitoringLeveeNavigator == null) {
			monitoringLeveeNavigator = eventBus.addHandler(LeveeNavigatorPresenter.class);
		}
		
		view.setMainPanelWidget(monitoringLeveeNavigator.getView());
	}
	
	public void onAnalysisPanel() {
		view.markAnalysisOption(true);
		view.markMonitoringOption(false);
		view.clearPanels();
		
		if(analysisPanelPresenter == null) {
			analysisPanelPresenter = eventBus.addHandler(AnalysisSidePanelPresenter.class);
		}
		
		view.setSidePanelWidget(analysisPanelPresenter.getView());
		analysisPanelPresenter.init();
		
		if(comparisonPresenter == null) {
			comparisonPresenter = eventBus.addHandler(ComparisonPresenter.class);
		}
		
		view.setMainPanelWidget(comparisonPresenter.getView());
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