package pl.ismop.web.client.widgets.newexperiment;

import pl.ismop.web.client.widgets.newexperiment.IExperimentView.IExperimentPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class ExperimentWidget extends Composite implements IExperimentView, ReverseViewInterface<IExperimentPresenter> {
	private static ExperimentWidgetUiBinder uiBinder = GWT.create(ExperimentWidgetUiBinder.class);
	interface ExperimentWidgetUiBinder extends UiBinder<Widget, ExperimentWidget> {}
	
	private IExperimentPresenter presenter;
	
	@UiField HTMLPanel panel;
	@UiField ExperimentMessages messages;
	@UiField Label profileLabel;
	@UiField TextBox name;
	@UiField(provided = true) ListBox days;
	@UiField Label errorLabel;
	
	public ExperimentWidget() {
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
	public void setPresenter(IExperimentPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IExperimentPresenter getPresenter() {
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
	public void clearErrorMessages() {
		errorLabel.setText("");
	}
}