package pl.ismop.web.client.widgets.old.experiments;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ThreatAssessmentCallback;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.old.experimentitem.ExperimentItemPresenter;
import pl.ismop.web.client.widgets.old.experiments.IExperimentsView.IExperimentsPresenter;

@Presenter(view = ExperimentsView.class)
public class ExperimentsPresenter extends BasePresenter<IExperimentsView, MainEventBus> implements IExperimentsPresenter {
	private DapController dapController;
	private List<String> experimentsIds;
	private Map<String, ExperimentItemPresenter> experimentItemPresenters;
	private Timer timer;

	@Inject
	public ExperimentsPresenter(DapController dapController) {
		this.dapController = dapController;
		experimentItemPresenters = new HashMap<>();
	}
	
	public void onShowExperiments(List<String> experimentsIds) {
		this.experimentsIds = experimentsIds;
		
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		
		view.clear();
		loadExperiments();
		//update();
	}

	private void update() {
		if(timer == null) {
			timer = new Timer() {
				@Override
				public void run() {
					timer = null;
					loadExperiments();
					update();
				}
			};
			timer.schedule(5000);
		}
	}

	private void loadExperiments() {
//		eventBus.popupClosed();
//		eventBus.setTitleAndShow(view.popupTitle(), view, false);
		
		if(experimentsIds.size() == 0) {
			view.showNoExperimentsMessage();
		} else {
			dapController.getExperiments(experimentsIds, new ThreatAssessmentCallback() {
				@Override
				public void onError(ErrorDetails errorDetails) {
					Window.alert("Error: " + errorDetails.getMessage());
				}
				
				@Override
				public void processExperiments(List<Experiment> experiments) {
					Collections.sort(experiments, new Comparator<Experiment>() {
						@Override
						public int compare(Experiment o1, Experiment o2) {
							return o1.getName().compareToIgnoreCase(o2.getName());
						}
					});
					
					for(Experiment experiment : experiments) {
						ExperimentItemPresenter presenter = experimentItemPresenters.get(experiment.getId());
						
						if(presenter == null) {
							presenter = eventBus.addHandler(ExperimentItemPresenter.class);
							experimentItemPresenters.put(experiment.getId(), presenter);
						}
						
						view.addExperiment(presenter.getView());
						presenter.setExperiment(experiment);
					}
				}
			});
		}
	}

	@Override
	public void onWidgetDetached() {
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}