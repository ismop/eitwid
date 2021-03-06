package pl.ismop.web.client.widgets.old.newexperiment;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.hypgen.HypgenController;
import pl.ismop.web.client.hypgen.HypgenController.ExperimentCallback;
import pl.ismop.web.client.internal.InternalExperimentController;
import pl.ismop.web.client.internal.InternalExperimentController.InternalExperimentCallback;
import pl.ismop.web.client.widgets.old.newexperiment.IThreatAssessmentView.IThreatAssessmentPresenter;

@Presenter(view = ThreatAssessmentWidget.class, multiple = true)
public class ThreatAssessmentPresenter extends BasePresenter<IThreatAssessmentView, MainEventBus> implements IThreatAssessmentPresenter {
	private DapController dapController;
	protected List<Section> currentProfiles;
	private HypgenController hypgenController;
	private InternalExperimentController internalExperimentController;

	@Inject
	public ThreatAssessmentPresenter(DapController dapController, HypgenController hypgenController, InternalExperimentController internalExperimentController) {
		this.dapController = dapController;
		this.hypgenController = hypgenController;
		this.internalExperimentController = internalExperimentController;
	}
	
	public void onAreaSelected(float top, float left, float bottom, float right) {
//		eventBus.popupClosed();
//		eventBus.setTitleAndShow(view.title(), view, false);
		dapController.getSections(top, left, bottom, right, new SectionsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				Window.alert("Error: " + errorDetails.getMessage());
			}
			
			@Override
			public void processSections(List<Section> profiles) {
				currentProfiles = profiles;
				
				if(profiles.size() > 0) {
					view.setPickedProfilesMessage(profiles.size());
				}
			}
		});
	}
	
	private void executeExperiment(String name, String days, List<String> profileIds) {
		hypgenController.startExperiment(name, profileIds, days, new ExperimentCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}

			@Override
			public void processExperiment(final Experiment experiment) {
				internalExperimentController.addExperiment(experiment.getId(), new InternalExperimentCallback() {
					@Override
					public void onError(int code, String message) {
						Window.alert("Error: " + message);
					}
					
					@Override
					public void experimentAdded() {
						view.showExperimentCreatedMessage();
//						eventBus.experimentCreated(experiment);
					}
				});
			}});
	}
	
	@Override
	public void onStartClicked() {
		view.clearMessages();
		
		String name = view.getName().getText();
		
		if(name.trim().isEmpty()) {
			view.showNameEmptyMessage();
			
			return;
		}
		
		List<String> profileIds = new ArrayList<>();
		
		for(Section profile : currentProfiles) {
			profileIds.add(profile.getId());
		}
		
		executeExperiment(name, view.getDaysValue(), profileIds);
	}
}