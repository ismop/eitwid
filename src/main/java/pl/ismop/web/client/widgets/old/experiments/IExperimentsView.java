package pl.ismop.web.client.widgets.old.experiments;

import com.google.gwt.user.client.ui.IsWidget;

import pl.ismop.web.client.widgets.old.experimentitem.IExperimentItemView;

public interface IExperimentsView extends IsWidget {
	public interface IExperimentsPresenter {
		void onWidgetDetached();
	}

	void showNoExperimentsMessage();
	void addExperiment(IExperimentItemView view);
	void clear();
	String popupTitle();
}