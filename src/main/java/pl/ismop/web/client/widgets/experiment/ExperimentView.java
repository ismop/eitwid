package pl.ismop.web.client.widgets.experiment;

import org.gwtbootstrap3.client.ui.PanelBody;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class ExperimentView extends Composite implements IExperimentView {
	private static ExperimentViewUiBinder uiBinder = GWT.create(ExperimentViewUiBinder.class);
	interface ExperimentViewUiBinder extends UiBinder<Widget, ExperimentView> {}
	
	@UiField ExperimentMessages messages;
	@UiField PanelBody analysisBody;

	public ExperimentView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public String getMainTitle() {
		return messages.mainTitle();
	}

	@Override
	public void addAnalysis(IsWidget view) {
		analysisBody.clear();
		analysisBody.add(view);
	}
}