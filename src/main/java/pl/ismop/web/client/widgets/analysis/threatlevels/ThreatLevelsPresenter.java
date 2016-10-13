package pl.ismop.web.client.widgets.analysis.threatlevels;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.threatlevel.ThreatAssessment;
import pl.ismop.web.client.dap.threatlevel.ThreatLevel;
import pl.ismop.web.client.widgets.analysis.threatlevels.IThreatLevelsView.IThreadLevelsPresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = ThreatLevelsView.class, multiple = true)
public class ThreatLevelsPresenter extends BasePresenter<IThreatLevelsView, MainEventBus>
		implements IPanelContent<IThreatLevelsView, MainEventBus>, IThreadLevelsPresenter {

	private Experiment experiment;
	private Map<String, ThreatLevel> namesToThreatLevels = new HashMap<>();

	@Override
	public void setSelectedExperiment(Experiment experiment) {
		this.experiment = experiment;
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
		ThreatLevel tl = namesToThreatLevels.get(profileName);
		if (tl != null && tl.getThreatAssessments() != null && tl.getThreatAssessments().size() > 0) {
			ThreatAssessment ta = tl.getThreatAssessments().get(0);
			view.showScenarios(ta.getScenarios());
		}
	}

	public void onThreatLevelsChanged(List<ThreatLevel> threatLevels) {
		view.clearScenarios();
		namesToThreatLevels.clear();
		if (threatLevels != null) {
			threatLevels.stream().forEach(tl -> namesToThreatLevels.put("Profile " + tl.getProfileId(), tl));
			view.setProfiles(new ArrayList<>(namesToThreatLevels.keySet()));
		}
		view.showResults();
	}
}
