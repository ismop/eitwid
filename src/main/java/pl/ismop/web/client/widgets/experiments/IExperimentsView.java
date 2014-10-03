package pl.ismop.web.client.widgets.experiments;

import java.util.Date;

import pl.ismop.web.client.widgets.experimentitem.IExperimentItemView;

import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentsView extends IsWidget {
	public interface IExperimentsPresenter {
		void showResults(String id);
	}

	void showNoExperimentsMessage();
	void addExperiment(IExperimentItemView view);
	void clear();
}