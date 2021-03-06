package pl.ismop.web.client.widgets.realtime.main;

import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.realtime.main.IRealTimePanelView.IRealTimePanelPresenter;

public class RealTimePanelView extends Composite implements IRealTimePanelView,
		ReverseViewInterface<IRealTimePanelPresenter> {
	private static RealTimePanelViewUiBinder uiBinder = GWT.create(RealTimePanelViewUiBinder.class);

	interface RealTimePanelViewUiBinder extends UiBinder<Widget, RealTimePanelView> {}
	
	@UiField
	RealTimePanelMessages messages;
	
	@UiField
	Text weatherSectionHeading, verticalSliceSectionHeading, horizontalSliceSectionHeading;
	
	@UiField
	HTML firstWeatherParameterName, firstWeatherParameterValue, secondWeatherParameterName,
			secondWeatherParameterValue, thirdWeatherParameterName, thirdWeatherParameterValue,
			fourthWeatherParameterName, fourthWeatherParameterValue, fifthWeatherParameterName,
			fifthWeatherParameterValue, sixthWeatherParameterName, sixthWeatherParameterValue,
			firstWeatherParameterDate, secondWeatherParameterDate, thirdWeatherParameterDate,
			fourthWeatherParameterDate,	fifthWeatherParameterDate, sixthWeatherParameterDate,
			waterLevelParameterValue, waterLevelParameterDate;
	
	@UiField
	FlowPanel chartContainer, verticalSliceContainer, horizontalSliceContainer;
	
	@UiField
	Icon loadingIndicator;

	private IRealTimePanelPresenter presenter;
	
	private List<HTML> weatherParameterNames;
	
	private List<HTML> weatherParameterValues;
	
	private List<HTML> weatherParameterDates;

	public RealTimePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		weatherParameterNames = Arrays.asList(firstWeatherParameterName, secondWeatherParameterName,
				thirdWeatherParameterName, fourthWeatherParameterName, fifthWeatherParameterName,
				sixthWeatherParameterName);
		weatherParameterValues = Arrays.asList(firstWeatherParameterValue,
				secondWeatherParameterValue, thirdWeatherParameterValue,
				fourthWeatherParameterValue, fifthWeatherParameterValue,
				sixthWeatherParameterValue);
		weatherParameterDates = Arrays.asList(firstWeatherParameterDate,
				secondWeatherParameterDate, thirdWeatherParameterDate,
				fourthWeatherParameterDate, fifthWeatherParameterDate,
				sixthWeatherParameterDate);
	}
	
	@UiHandler("changeWeatherSource")
	public void changeWeatherSource(ClickEvent event) {
		getPresenter().onWeatherSourceChange();
	}
	
	@UiHandler("changeVerticalSliceParameter")
	public void changeVerticalSliceParameter(ClickEvent event) {
		getPresenter().onVerticalSliceParameterChange();
	}
	
	@UiHandler("changeHorizontalSliceParameter")
	public void changeHorizontalSliceParameter(ClickEvent event) {
		getPresenter().onHorizontalSliceParameterChange();
	}

	@Override
	public void setPresenter(IRealTimePanelPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IRealTimePanelPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setWeatherSectionTitle(String weatherDeviceName) {
		weatherSectionHeading.setText(messages.weatherStation(weatherDeviceName));
	}

	@Override
	public void setWeatherParameter(int index, String name, String value, String date) {
		if (index < weatherParameterNames.size() && index < weatherParameterValues.size()
				&& index < weatherParameterDates.size()) {
			weatherParameterNames.get(index).setText(name);
			weatherParameterValues.get(index).setText(value);
			weatherParameterDates.get(index).setText(date);
		}
	}

	@Override
	public void setChartView(IsWidget chartView) {
		chartContainer.add(chartView);
	}

	@Override
	public void showLoadingIndicator(boolean show) {
		loadingIndicator.setVisible(show);
	}

	@Override
	public void setEmptyWaterLevelValues() {
		waterLevelParameterValue.setText(messages.emptyValue());
	}

	@Override
	public void setWaterLevelValue(String value) {
		waterLevelParameterValue.setText(value);
	}

	@Override
	public void setWaterLevelDate(String date) {
		waterLevelParameterDate.setText(date);
	}

	@Override
	public void setVerticalSliceView(IsWidget view) {
		verticalSliceContainer.add(view);
	}

	@Override
	public void setVerticalSliceHeading(String parameterName) {
		verticalSliceSectionHeading.setText(messages.verticalSliceSectionHeading(parameterName));
	}

	@Override
	public void setHorizontalSliceView(IsWidget view) {
		horizontalSliceContainer.add(view);
	}

	@Override
	public void setHorizontalSliceHeading(String parameterMeasurementName) {
		horizontalSliceSectionHeading.setText(messages.horizontalSliceSectionHeading(
				parameterMeasurementName));
	}
}