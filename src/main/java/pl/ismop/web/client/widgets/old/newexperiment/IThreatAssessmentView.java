package pl.ismop.web.client.widgets.old.newexperiment;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface IThreatAssessmentView extends IsWidget {
	interface IThreatAssessmentPresenter {
		void onStartClicked();
	}

	String startExperimentButtonLabelMessage();
	String getDaysValue();
	void setPickedProfilesMessage(int size);
	void showNameEmptyMessage();
	HasText getName();
	void clearMessages();
	String title();
	void showExperimentCreatedMessage();
}