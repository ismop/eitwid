package pl.ismop.web.client.widgets.experiments;

import pl.ismop.web.client.widgets.experimentitem.IExperimentItemView;

import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentsView extends IsWidget {
	public interface IExperimentsPresenter {
		void onWidgetDetached();
	}

	void showNoExperimentsMessage();
	void addExperiment(IExperimentItemView view);
	void clear();
}