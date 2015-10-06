package pl.ismop.web.client.widgets.analysis.sidepanel;

import com.google.gwt.user.client.ui.IsWidget;
import pl.ismop.web.client.dap.experiment.Experiment;

import java.util.List;

public interface IAnalysisSidePanelView extends IsWidget {
	int getWaterWavePanelHeight();

	void setExperiments(List<Experiment> experiments);

	void setWaterWavePanel(IsWidget widget);

	void setMinimap(IsWidget widget);

	interface IAnalysisSidePanelPresenter {
		void selectExperiment(Experiment selectedExperiment);
	}
}
