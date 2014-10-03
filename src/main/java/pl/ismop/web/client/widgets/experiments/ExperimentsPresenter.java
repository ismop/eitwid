package pl.ismop.web.client.widgets.experiments;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ExperimentsCallback;
import pl.ismop.web.client.hypgen.Experiment;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = ExperimentsView.class)
public class ExperimentsPresenter extends BasePresenter<IExperimentsView, MainEventBus>{
	private DapController dapController;
	private List<String> experimentsIds;

	@Inject
	public ExperimentsPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowExperiments(List<String> experimentsIds) {
		this.experimentsIds = experimentsIds;
		DOM.getElementById("page-wrapper").removeAllChildren();
		RootPanel.get("page-wrapper").add(view);
		loadExperiments();
	}

	private void loadExperiments() {
		if(experimentsIds.size() == 0) {
			view.showNoExperimentsMessage();
		} else {
			dapController.getExperiments(experimentsIds, new ExperimentsCallback() {
				@Override
				public void onError(int code, String message) {
					Window.alert("Error: " + message);
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
						view.addExperiment(experiment.getName());
					}
					
				}
			});
		}
	}
}