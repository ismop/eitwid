package pl.ismop.web.client.widgets.root;

import com.google.gwt.user.client.Timer;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.monitoring.sidepanel.MonitoringSidePanelPresenter;
import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;

@Presenter(view = RootPanel.class)
public class RootPresenter extends BasePresenter<IRootPanelView, MainEventBus> implements IRootPresenter{
	private MonitoringSidePanelPresenter monitoringSidePanelPresenter;
	private MapPresenter monitoringMapPresenter;

	public void onMonitoringPanel() {
		view.markAnalysisOption(false);
		view.markMonitoringOption(true);
		view.clearPanels();
		
		if(monitoringSidePanelPresenter == null) {
			monitoringSidePanelPresenter = eventBus.addHandler(MonitoringSidePanelPresenter.class);
		}
		
		monitoringSidePanelPresenter.reset();
		view.setSidePanelWidget(monitoringSidePanelPresenter.getView());
		
		if(monitoringMapPresenter == null) {
			monitoringMapPresenter = eventBus.addHandler(MapPresenter.class);
		}
		
		view.setMainPanelWidget(monitoringMapPresenter.getView());
	}
	
	public void onAnalysisPanel() {
		view.markAnalysisOption(true);
		view.markMonitoringOption(false);
		view.clearPanels();
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