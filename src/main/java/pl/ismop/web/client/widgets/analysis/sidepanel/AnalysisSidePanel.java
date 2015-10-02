package pl.ismop.web.client.widgets.analysis.sidepanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class AnalysisSidePanel extends Composite implements IAnalysisSidePanel {
	private static AnalysisSidePanelUiBinder uiBinder = GWT.create(AnalysisSidePanelUiBinder.class);

	interface AnalysisSidePanelUiBinder extends UiBinder<Widget, AnalysisSidePanel> {}

	public AnalysisSidePanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	FlowPanel waterWavePanel;

	@UiField
	FlowPanel miniMapPanel;

	@UiField
	FlowPanel actionsPanel;

	@Override
	public void setWaterWavePanel(IsWidget widget) {
		waterWavePanel.add(widget);
	}

	@Override
	public void setMinimap(IsWidget widget) {
		miniMapPanel.add(widget);
	}

	@Override
	public void addAction(IsWidget widget) {
		actionsPanel.add(widget);
	}

	@Override
	public int getWaterWavePanelWidth() {
		return waterWavePanel.getOffsetWidth();
	}

	@Override
	public int getWaterWavePanelHeight() {
		return waterWavePanel.getOffsetHeight();
	}
}