package pl.ismop.web.client.widgets.newexperiment;

import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;

import pl.ismop.web.client.widgets.newexperiment.IThreatAssessmentView.IThreatAssessmentPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class ThreatAssessmentWidget extends Composite implements IThreatAssessmentView, ReverseViewInterface<IThreatAssessmentPresenter> {
	private static ThreatAssessmentWidgetUiBinder uiBinder = GWT.create(ThreatAssessmentWidgetUiBinder.class);
	interface ThreatAssessmentWidgetUiBinder extends UiBinder<Widget, ThreatAssessmentWidget> {}
	
	private IThreatAssessmentPresenter presenter;
	
	@UiField HTMLPanel panel;
	@UiField ThreatAssessmentMessages messages;
	@UiField Label profileLabel;
	@UiField TextBox name;
	@UiField(provided = true) ListBox days;
	@UiField Label errorLabel;
	@UiField Label successLabel;
	
	public ThreatAssessmentWidget() {
		days = new ListBox();
		
		for(int i = 0; i < 14; i++) {
			days.addItem("" + (i + 1));
		}
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("startButton")
	void startClicked(ClickEvent event) {
		getPresenter().onStartClicked();
	}

	@Override
	public String startExperimentButtonLabelMessage() {
		return messages.startExperimentButtonLabel();
	}

	@Override
	public String getDaysValue() {
		return days.getValue(days.getSelectedIndex());
	}

	@Override
	public void setPickedProfilesMessage(int size) {
		profileLabel.setText(messages.pickedProfiles(size));
	}

	@Override
	public void setPresenter(IThreatAssessmentPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IThreatAssessmentPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void showNameEmptyMessage() {
		errorLabel.setText(messages.emptyName());
	}

	@Override
	public HasText getName() {
		return name;
	}

	@Override
	public void clearMessages() {
		errorLabel.setText("");
		successLabel.setText("");
	}

	@Override
	public String title() {
		return messages.leveesExperimentModalTitle();
	}

	@Override
	public void showExperimentCreatedMessage() {
		successLabel.setText(messages.experimentCreated());
	}
}