package pl.ismop.web.client.widgets.monitoring.weather;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Description;
import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.client.ui.DescriptionTitle;
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
import com.google.gwt.user.client.ui.Widget;

public class WeatherStationView extends Composite implements IWeatherStationView {
	private static WeatherStationViewUiBinder uiBinder = GWT.create(WeatherStationViewUiBinder.class);

	interface WeatherStationViewUiBinder extends UiBinder<Widget, WeatherStationView> {}
	
	@UiField WeatherStationViewMessages messages;
	@UiField Modal modal;
	@UiField FlowPanel progress;
	@UiField Heading weatherHeading1;
	@UiField Heading weatherHeading2;
	@UiField FlowPanel chartSlot;
	@UiField FlowPanel measurements1;
	@UiField FlowPanel measurements2;
	@UiField Container container;

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
	public void setChart(StockChart firstChart) {
		chartSlot.add(firstChart);
	}

	@Override
	public HasVisibility getChartVisibility() {
		return chartSlot;
	}

	@Override
	public HasText getHeading1() {
		return weatherHeading1;
	}

	@Override
	public HasText getHeading2() {
		return weatherHeading2;
	}

	@Override
	public HasVisibility getContentVisibility() {
		return container;
	}

	@Override
	public void addLatestReading1(String label, String value, String unit, String timestamp) {
		measurements1.add(createDescription(label, value, unit, timestamp));
	}

	@Override
	public void clearMeasurements() {
		measurements1.clear();
		measurements2.clear();
	}

	@Override
	public void addLatestReading2(String label, String value, String unit, String timestamp) {
		measurements2.add(createDescription(label, value, unit, timestamp));
	}
	
	private Description createDescription(String label, String value, String unit, String timestamp) {
		Description description = new Description();
		description.setHorizontal(true);
		
		DescriptionTitle title = new DescriptionTitle();
		title.setText(label);
		description.add(title);
		
		DescriptionData data = new DescriptionData();
		data.setText(value + " " + unit);
		description.add(data);
		data.setTitle(timestamp);
		
		return description;
	}
}