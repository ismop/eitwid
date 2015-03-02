package pl.ismop.web.client.widgets.experimentitem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import pl.ismop.web.client.widgets.experimentitem.IExperimentItemView.IExperimentItemPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class ExperimentItemView extends Composite implements IExperimentItemView, ReverseViewInterface<IExperimentItemPresenter> {
	private static ExperimentItemViewUiBinder uiBinder = GWT.create(ExperimentItemViewUiBinder.class);
	interface ExperimentItemViewUiBinder extends UiBinder<Widget, ExperimentItemView> {}
	
	interface Style extends CssResource {
		String headerRow();
	}

	private DateTimeFormat format;
	
	@UiField Style style;
	@UiField Label name;
	@UiField Label status;
	@UiField ExperimentItemMessages messages;
	@UiField Button collapseButton;
	@UiField Collapse collapse;
	@UiField Label comparisonDates;
	@UiField FlowPanel results;

	private IExperimentItemPresenter presenter;
	
	public ExperimentItemView() {
		format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("collapseButton")
	void collapse(ClickEvent event) {
		collapse.toggle();
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

	@Override
	public void setDates(Date start, Date end) {
		String startDateValue = start == null ? messages.emptyDate() : format.format(start);
		String endDateValue = end == null ? messages.emptyDate() : format.format(end);
		comparisonDates.setText(startDateValue + " - " + endDateValue);
	}

	@Override
	public void showNoResultsLabel() {
		results.clear();
		results.add(new org.gwtbootstrap3.client.ui.Label(messages.noResultsMessage()));
	}

	@Override
	public void showResultsLabel() {
		results.clear();
		results.add(new Label(messages.resultsHeader()));
	}

	@Override
	public void addResultItem(Map<String, String> threatLevels) {
		FlowPanel resultItem = new FlowPanel();
		List<String> keys = new ArrayList<>(threatLevels.keySet());
		Collections.sort(keys);
		
		for(final String sectionId : keys) {
			Button sectionButton = new Button(sectionId, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					getPresenter().onShowSection(sectionId);
				}
			});
			sectionButton.setType(getButtonType(threatLevels.get(sectionId)));
			resultItem.add(sectionButton);
		}
		
		results.add(resultItem);
	}

	private ButtonType getButtonType(String threatLevel) {
		switch(threatLevel) {
			case "none":
				return ButtonType.SUCCESS;
			case "heightened":
				return ButtonType.WARNING;
			case "severe":
				return ButtonType.DANGER;
			default:
				return ButtonType.DEFAULT;
		}
	}
}