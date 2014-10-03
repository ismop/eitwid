package pl.ismop.web.client.widgets.newexperiment;

import java.util.ArrayList;
import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.hypgen.HypgenController;
import pl.ismop.web.client.hypgen.HypgenController.ExperimentCallback;
import pl.ismop.web.client.internal.InternalExperimentController;
import pl.ismop.web.client.internal.InternalExperimentController.InternalExperimentCallback;
import pl.ismop.web.client.widgets.newexperiment.IExperimentView.IExperimentPresenter;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = ExperimentWidget.class, multiple = true)
public class ExperimentPresenter extends BasePresenter<IExperimentView, MainEventBus> implements IExperimentPresenter {
	private DapController dapController;
	private boolean modalInitialized;
	protected List<Profile> currentProfiles;
	private HypgenController hypgenController;
	private InternalExperimentController internalExperimentController;

	@Inject
	public ExperimentPresenter(DapController dapController, HypgenController hypgenController, InternalExperimentController internalExperimentController) {
		this.dapController = dapController;
		this.hypgenController = hypgenController;
		this.internalExperimentController = internalExperimentController;
	}
	
	public void onAreaSelected(float top, float left, float bottom, float right) {
		RootPanel.get("modalContainer").clear();
		RootPanel.get("modalContainer").add(view);
		dapController.getProfiles(top, left, bottom, right, new ProfilesCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processProfiles(List<Profile> profiles) {
				currentProfiles = profiles;
				
				if(profiles.size() > 0) {
					view.setPickedProfilesMessage(profiles.size());
					showModal();
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
						eventBus.experimentCreated(experiment);
					}
				});
			}});
	}
	
	@Override
	public void onStartClicked() {
		view.clearErrorMessages();
		
		String name = view.getName().getText();
		
		if(name.trim().isEmpty()) {
			view.showNameEmptyMessage();
			
			return;
		}
		
		List<String> profileIds = new ArrayList<>();
		
		for(Profile profile : currentProfiles) {
			profileIds.add(profile.getId());
		}
		
		executeExperiment(name, view.getDaysValue(), profileIds);
		hideModal();
	}
	
	private native void hideModal() /*-{
		$wnd.jQuery('#experiment-modal').modal('hide');
	}-*/;

	private native void showModal() /*-{
		$wnd.jQuery('#experiment-modal').modal('show');
	}-*/;
}