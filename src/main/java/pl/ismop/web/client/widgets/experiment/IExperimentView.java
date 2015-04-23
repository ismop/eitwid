package pl.ismop.web.client.widgets.experiment;

import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentView extends IsWidget {
	interface IExperimentPresenter {
		
	}

	String getMainTitle();
	void addAnalysis(IsWidget view);
}