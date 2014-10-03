package pl.ismop.web.client.widgets.experiments;

import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentsView extends IsWidget {
	public interface IExperimentsPresenter {
		
	}

	void addExperiment(String name);
	void showNoExperimentsMessage();
}