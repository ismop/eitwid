package pl.ismop.web.client.widgets.analysis.sidepanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AnalysisSidePanel extends Composite implements IAnalysisSidePanel {
	private static AnalysisSidePanelUiBinder uiBinder = GWT.create(AnalysisSidePanelUiBinder.class);

	interface AnalysisSidePanelUiBinder extends UiBinder<Widget, AnalysisSidePanel> {}

	public AnalysisSidePanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}