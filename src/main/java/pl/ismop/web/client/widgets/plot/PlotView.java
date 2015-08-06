package pl.ismop.web.client.widgets.plot;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class PlotView extends Composite implements IPlotView {
	private static PlotViewUiBinder uiBinder = GWT.create(PlotViewUiBinder.class);
	
	interface PlotViewUiBinder extends UiBinder<Widget, PlotView> {}

	@UiField PlotMessages messages;
	
	@UiField HTML messageLabel;
	
	@UiField FlowPanel container, plotBusyPanel;
	
	public PlotView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showMessageLabel(boolean show) {
		messageLabel.setVisible(show);
	}

	@Override
	public void setNoParamtersMessage() {
		messageLabel.setText(messages.noParameterLabel());
	}

	@Override
	public void setNoContextsMessage() {
		messageLabel.setText(messages.noContextsLabel());
	}

	@Override
	public void setNoTimelinesMessage() {
		messageLabel.setText(messages.noTimelinesLabel());
	}

	@Override
	public void setNoMeasurementsMessage() {
		messageLabel.setText(messages.noMeasurementsLabel());
	}

	@Override
	public void setPlot(IsWidget widget) {
		container.clear();
		container.add(widget);
	}

	@Override
	public void showBusyPanel(boolean busy) {
		plotBusyPanel.setVisible(busy);
	}
}