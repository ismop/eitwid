package pl.ismop.web.client.widgets.experiment;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentView extends IsWidget {
	interface IExperimentPresenter {
		void showPlot();
	}

	String getMainTitle();
	void addAnalysis(IsWidget view);
	void addPlot(FlowPanel panel);
	void showFirstWave();
}