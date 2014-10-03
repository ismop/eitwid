package pl.ismop.web.client.widgets.experiments;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ExperimentsView extends Composite implements IExperimentsView {
	private static ExperimentsViewUiBinder uiBinder = GWT.create(ExperimentsViewUiBinder.class);
	interface ExperimentsViewUiBinder extends UiBinder<Widget, ExperimentsView> {}
	
	@UiField HTMLPanel experimentContainer;
	@UiField ExperimentsMessages messages;

	public ExperimentsView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void addExperiment(String name) {
		experimentContainer.add(new Label(name));
	}

	@Override
	public void showNoExperimentsMessage() {
		experimentContainer.add(new Label(messages.noExperiments()));
	}
}