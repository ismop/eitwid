package pl.ismop.web.client.widgets.experiment;

import static java.util.Arrays.asList;

import java.util.List;

import javax.inject.Inject;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ExperimentsCallback;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.experimentitem.ExperimentItemPresenter;

import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = ExperimentView.class)
public class ExperimentPresenter extends BasePresenter<IExperimentView, MainEventBus> {
	private DapController dapController;
	private ExperimentItemPresenter presenter;

	@Inject
	public ExperimentPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowExperiment() {
		eventBus.setTitleAndShow(view.getMainTitle(), view, false);
		dapController.getExperiments(asList("12"), new ExperimentsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processExperiments(List<Experiment> experiments) {
				if(presenter != null) {
					eventBus.removeHandler(presenter);
				}
				
				presenter = eventBus.addHandler(ExperimentItemPresenter.class);
				presenter.setExperiment(experiments.get(0));
				view.addAnalysis(presenter.getView());
			}
		});
	}
}