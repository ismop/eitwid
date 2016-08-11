package pl.ismop.web.client.widgets.analysis.threatlevels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.widgets.analysis.threatlevels.IThreatLevelsView.IThreadLevelsPresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = ThreatLevelsView.class, multiple = true)
public class ThreatLevelsPresenter extends BasePresenter<IThreatLevelsView, MainEventBus>
		implements IPanelContent<IThreatLevelsView, MainEventBus>, IThreadLevelsPresenter {

	private Experiment experiment;		
	
	
	@Override
	public void setSelectedExperiment(Experiment experiment) {
		this.experiment = experiment;				
	}	

	@Override
	public void bind() {
		view.setProfiles(Arrays.asList("Profile 1", "Profile 2", "Profile 3", "Profile 4"));
		view.showResults();
	}

	@Override
	public void setSelectedDate(Date date) {
	}

	@Override
	public void edit() {
	}

	@Override
	public void setSelectionManager(ISelectionManager selectionManager) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void changeProfile(String profileName) {
		view.clearScenarios();
		List<Scenario> scenarios = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			Scenario s = new Scenario();
			s.setName(profileName + " - scenario " + (i + 1));
			s.setDescription(profileName + " - scenario description " + (i + 1));
			s.setThreatLevel(i % 3);
			
			scenarios.add(s);
		}
		
		view.showScenarios(scenarios);
	}
	
	public void onThreatLevelsChanged(String msg) {
		GWT.log("Changing threat levels list" + msg);
	}
}
