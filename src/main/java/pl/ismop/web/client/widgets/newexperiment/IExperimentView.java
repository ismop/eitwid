package pl.ismop.web.client.widgets.newexperiment;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentView extends IsWidget {
	interface IExperimentPresenter {
		void onStartClicked();
	}

	String startExperimentButtonLabelMessage();
	String getDaysValue();
	void setPickedProfilesMessage(int size);
	void showNameEmptyMessage();
	HasText getName();
	void clearErrorMessages();
}