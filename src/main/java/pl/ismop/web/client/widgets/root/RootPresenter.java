package pl.ismop.web.client.widgets.root;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.MalfunctioningParametersCallback;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.comparison.ComparisonPresenter;
import pl.ismop.web.client.widgets.analysis.sidepanel.AnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.monitoring.mapnavigator.LeveeNavigatorPresenter;
import pl.ismop.web.client.widgets.monitoring.sidepanel.MonitoringSidePanelPresenter;
import pl.ismop.web.client.widgets.realtime.main.RealTimePanelPresenter;
import pl.ismop.web.client.widgets.realtime.side.RealTimeSidePanelPresenter;
import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;

@Presenter(view = RootPanel.class)
public class RootPresenter extends BasePresenter<IRootPanelView, MainEventBus> implements IRootPresenter{
	private MonitoringSidePanelPresenter monitoringSidePanelPresenter;
	
	private LeveeNavigatorPresenter monitoringLeveeNavigator;

	private AnalysisSidePanelPresenter analysisPanelPresenter;
	
	private ComparisonPresenter comparisonPresenter;
	
	private RealTimePanelPresenter realTimePanelPresenter;
	
	private RealTimeSidePanelPresenter realTimeSidePanelPresenter;
	
	private DapController dapController;
	
	private List<Parameter> malfunctioningParameters;
	
	@Inject
	public RootPresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void onMonitoringPanel() {
		refreshBrokenDevicesLink();
		view.markAnalysisOption(false);
		view.markMonitoringOption(true);
		view.markRealTimeOption(false);
		view.clearPanels();
		
		if (monitoringSidePanelPresenter == null) {
			monitoringSidePanelPresenter = eventBus.addHandler(MonitoringSidePanelPresenter.class);
		}
		
		monitoringSidePanelPresenter.reset();
		view.setSidePanelWidget(monitoringSidePanelPresenter.getView());
		
		if (monitoringLeveeNavigator == null) {
			monitoringLeveeNavigator = eventBus.addHandler(LeveeNavigatorPresenter.class);
		}
		
		view.setMainPanelWidget(monitoringLeveeNavigator.getView());
		
		if (realTimeSidePanelPresenter != null) {
			realTimeSidePanelPresenter.disableTimers();
		}
		
		if (realTimePanelPresenter != null) {
			realTimePanelPresenter.cleanUp();
		}
	}
	
	public void onAnalysisPanel() {
		refreshBrokenDevicesLink();
		view.markAnalysisOption(true);
		view.markMonitoringOption(false);
		view.markRealTimeOption(false);
		view.clearPanels();
		
		if (analysisPanelPresenter == null) {
			analysisPanelPresenter = eventBus.addHandler(AnalysisSidePanelPresenter.class);
		}
		
		view.setSidePanelWidget(analysisPanelPresenter.getView());
		analysisPanelPresenter.init();
		
		if (comparisonPresenter == null) {
			comparisonPresenter = eventBus.addHandler(ComparisonPresenter.class);
		}

		view.setMainPanelWidget(comparisonPresenter.getView());
		comparisonPresenter.init();
		
		if (realTimeSidePanelPresenter != null) {
			realTimeSidePanelPresenter.disableTimers();
		}
		
		if (realTimePanelPresenter != null) {
			realTimePanelPresenter.cleanUp();
		}
	}
	
	public void onRealTimePanel() {
		refreshBrokenDevicesLink();
		view.markAnalysisOption(false);
		view.markMonitoringOption(false);
		view.markRealTimeOption(true);
		view.clearPanels();
		
		if (realTimeSidePanelPresenter == null) {
			realTimeSidePanelPresenter = eventBus.addHandler(RealTimeSidePanelPresenter.class);
		}
		
		view.setSidePanelWidget(realTimeSidePanelPresenter.getView());
		realTimeSidePanelPresenter.init();
		
		if (realTimePanelPresenter == null) {
			realTimePanelPresenter = eventBus.addHandler(RealTimePanelPresenter.class);
		}

		view.setMainPanelWidget(realTimePanelPresenter.getView());
		realTimePanelPresenter.init();
	}

	@Override
	public void onMonitoringViewOption() {
		eventBus.monitoringPanel();
	}

	@Override
	public void onAnalysisViewOption() {
		eventBus.analysisPanel();
	}

	@Override
	public void onRealTimeViewOption() {
		eventBus.realTimePanel();
	}

	@Override
	public void onBrokenDevicesClicked() {
		List<String> brokenParameters = new ArrayList<String>();
		
		for(Parameter parameter : malfunctioningParameters) {
			brokenParameters.add(parameter.getCustomId() + " (" + parameter.getMeasurementTypeName() + ")");
		}
		
		Collections.sort(brokenParameters);
		view.showDetails(brokenParameters);
	}

	private void refreshBrokenDevicesLink() {
		dapController.getMalfunctioningParameters(new MalfunctioningParametersCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processMalfunctioningParameters(List<Parameter> malfunctioningParameters) {
				RootPresenter.this.malfunctioningParameters = malfunctioningParameters;
				view.setBrokenDevicesLinkLabel(malfunctioningParameters.size());
				view.showBrokenDevicesLink(true, malfunctioningParameters.size() > 0);
			}
		});
	}
}