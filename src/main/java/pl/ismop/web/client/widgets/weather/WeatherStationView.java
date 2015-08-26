package pl.ismop.web.client.widgets.weather;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.moxieapps.gwt.highcharts.client.StockChart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class WeatherStationView extends Composite implements IWeatherStationView {
	private static WeatherStationViewUiBinder uiBinder = GWT.create(WeatherStationViewUiBinder.class);

	interface WeatherStationViewUiBinder extends UiBinder<Widget, WeatherStationView> {}
	
	@UiField WeatherStationViewMessages messages;
	@UiField Modal modal;
	@UiField FlowPanel progress1;
	@UiField FlowPanel progress2;
	@UiField Heading weatherHeading1;
	@UiField Heading weatherHeading2;
	@UiField Container dataContainer;
	@UiField FlowPanel firstChartSlot;
	@UiField FlowPanel secondChartSlot;
	@UiField Label noDataMessage1;
	@UiField Label noDataMessage2;

	public WeatherStationView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showModal() {
		modal.show();
	}

	@Override
	public void showProgress1(boolean show) {
		progress1.setVisible(show);
	}
	
	@Override
	public void showProgress2(boolean show) {
		progress2.setVisible(show);
	}

	@Override
	public HasText getSecondHeading() {
		return weatherHeading2;
	}

	@Override
	public HasText getFirstHeading() {
		return weatherHeading1;
	}

	@Override
	public void setFirstChart(StockChart firstChart) {
		firstChartSlot.add(firstChart);
	}

	@Override
	public HasVisibility getFirstNoDataMessage() {
		return noDataMessage1;
	}

	@Override
	public HasVisibility getFirstProgress() {
		return progress1;
	}

	@Override
	public HasVisibility getSecondNoDataMessage() {
		return noDataMessage2;
	}

	@Override
	public HasVisibility getSecondProgress() {
		return progress2;
	}

	@Override
	public HasVisibility getSecondChartVisibility() {
		return secondChartSlot;
	}

	@Override
	public HasVisibility getFirstChartVisibility() {
		return firstChartSlot;
	}
}