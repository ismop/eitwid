package pl.ismop.web.client.widgets.analysis.threatlevels;

import java.util.List;

import pl.ismop.web.client.dap.threatlevel.Scenario;

public interface IThreatLevelsView {
	interface IThreadLevelsPresenter {
		void changeProfile(String value);
	}

	void clearScenarios();

	void loading(String msg);

	void showResults();

	void setProfiles(List<String> names);

	void showScenarios(List<Scenario> list);
}
