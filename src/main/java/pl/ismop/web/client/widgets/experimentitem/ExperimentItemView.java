package pl.ismop.web.client.widgets.experimentitem;

import java.util.Date;

import pl.ismop.web.client.widgets.experimentitem.IExperimentItemView.IExperimentItemPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class ExperimentItemView extends Composite implements IExperimentItemView, ReverseViewInterface<IExperimentItemPresenter> {
	private static ExperimentItemViewUiBinder uiBinder = GWT.create(ExperimentItemViewUiBinder.class);
	interface ExperimentItemViewUiBinder extends UiBinder<Widget, ExperimentItemView> {}

	private DateTimeFormat format;
	
	@UiField Label name;
	@UiField Label status;
	@UiField HTML startDate;
	@UiField HTML endDate;
	@UiField ExperimentItemMessages messages;

	private IExperimentItemPresenter presenter;
	
	public ExperimentItemView() {
		format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("showResults")
	void showResults(ClickEvent event) {
		getPresenter().onShowResults();
	}

	@Override
	public HasText getName() {
		return name;
	}

	@Override
	public void setStatus(String statusValue) {
		status.setText(statusValue);
		status.setStyleName(getStyleName(statusValue));
	}

	@Override
	public void setStartDate(Date date) {
		String startDateValue = date == null ? messages.emptyDate() : format.format(date);
		startDate.setHTML(messages.startDate(startDateValue));
	}

	@Override
	public void setEndDate(Date date) {
		String endDateValue = date == null ? messages.emptyDate() : format.format(date);
		endDate.setHTML(messages.endDate(endDateValue));
	}
	
	@Override
	public void setPresenter(IExperimentItemPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IExperimentItemPresenter getPresenter() {
		return presenter;
	}
	
	private String getStyleName(String status) {
		switch(status) {
			case "started":
				return "label label-primary";
			case "error":
				return "label label-danger";
			case "finished":
				return "label label-success";
			default:
				return "label label-default";
		}
	}

	@Override
	public String getSimilarityLabel() {
		return messages.similarityLabel();
	}

	@Override
	public String getProfileIdLabel() {
		return messages.profileIdLabel();
	}

	@Override
	public String getNoResultsMessage() {
		return messages.noResultsMessage();
	}
}