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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class WeatherStationView extends Composite implements IWeatherStationView {
	private static WeatherStationViewUiBinder uiBinder = GWT.create(WeatherStationViewUiBinder.class);

	interface WeatherStationViewUiBinder extends UiBinder<Widget, WeatherStationView> {}
	
	@UiField WeatherStationViewMessages messages;
	@UiField Modal modal;
	@UiField FlowPanel message;
	@UiField FlowPanel progress;
	@UiField Heading weatherHeading1;
	@UiField Heading weatherHeading2;
	@UiField Container dataContainer;
	@UiField FlowPanel firstChartSlot;
	@UiField FlowPanel secondChartSlot;

	public WeatherStationView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showModal() {
		modal.show();
	}

	@Override
	public void showProgress(boolean show) {
		progress.setVisible(show);
	}

	@Override
	public void showNoWeatherStationData(boolean show) {
		message.clear();
		message.add(new Label(messages.noWeatherData()));
		message.setVisible(show);
	}

	@Override
	public void clearMessage() {
		message.clear();
		message.setVisible(false);
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
	public void showDataContainer(boolean show) {
		dataContainer.setVisible(show);
	}

	@Override
	public void setFirstChart(StockChart firstChart) {
		firstChartSlot.add(firstChart);
	}

	@Override
	public void setSecondChart(StockChart secondChart) {
		secondChartSlot.add(secondChart);
	}
}