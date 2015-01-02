package pl.ismop.web.client.widgets.root;

import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ExperimentsCallback;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.internal.InternalExperimentController;
import pl.ismop.web.client.internal.InternalExperimentController.UserExperimentsCallback;
import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = RootPanel.class)
public class RootPresenter extends BasePresenter<IRootPanelView, MainEventBus> implements IRootPresenter{
	private DapController dapController;
	private InternalExperimentController internalExperimentController;
	private int numberOfExperiments;
	private List<String> experimentIds;
	
	@Inject
	public RootPresenter(DapController dapController, InternalExperimentController internalExperimentController) {
		this.dapController = dapController;
		this.internalExperimentController = internalExperimentController;
	}
	
	public void onStart() {
		RootLayoutPanel.get().add(view);
		eventBus.drawGoogleMap("mapPanel");
		internalExperimentController.getExperiments(new UserExperimentsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processUserExperiments(List<String> experimentIds) {
				RootPresenter.this.experimentIds = experimentIds;
				numberOfExperiments = experimentIds.size();
				
				if(numberOfExperiments == 0) {
					view.setExperimentsLabel(numberOfExperiments);
				} else {
					dapController.getExperiments(experimentIds, new ExperimentsCallback() {
						@Override
						public void onError(int code, String message) {
							Window.alert("Error: " + message);
						}
						
						@Override
						public void processExperiments(List<Experiment> experiments) {
							view.setExperimentsLabel(numberOfExperiments);
						}
					});
				}
			}

		});
	}
	
	public void onExperimentCreated(Experiment experiment) {
		numberOfExperiments++;
		experimentIds.add(experiment.getId());
		view.setExperimentsLabel(numberOfExperiments);
	}

	@Override
	public void onShowSensors(boolean show) {
		eventBus.showSensors(show);
	}

	@Override
	public void onShowLevees(boolean show) {
		eventBus.showLevees(show);
	}

	@Override
	public void onShowExperiments() {
		eventBus.showExperiments(experimentIds);
	}
}