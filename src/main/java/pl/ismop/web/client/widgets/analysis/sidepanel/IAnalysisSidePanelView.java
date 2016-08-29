package pl.ismop.web.client.widgets.analysis.sidepanel;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

import pl.ismop.web.client.dap.experiment.Experiment;

public interface IAnalysisSidePanelView extends IsWidget {
	int getWaterWavePanelHeight();

	void setExperiments(List<Experiment> experiments);

	void setRefresher(IsWidget widget);
	
	void setWaterWavePanel(IsWidget widget);

	void setMinimap(IsWidget widget);

	void selectExperiment(Experiment currentExperiment);

	AnalysisSidePanelMessages getMessages();

	void clearRefresher();
	
	interface IAnalysisSidePanelPresenter {
		void selectExperiment(Experiment selectedExperiment);
		void export();
	}
}
