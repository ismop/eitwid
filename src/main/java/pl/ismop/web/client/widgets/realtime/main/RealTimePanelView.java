package pl.ismop.web.client.widgets.realtime.main;

import java.util.Arrays;
import java.util.List;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
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
	Text weatherSectionHeading;
	
	@UiField
	HTML firstWeatherParameterName, firstWeatherParameterValue, secondWeatherParameterName,
			secondWeatherParameterValue, thirdWeatherParameterName, thirdWeatherParameterValue,
			fourthWeatherParameterName, fourthWeatherParameterValue, fifthWeatherParameterName,
			fifthWeatherParameterValue, sixthWeatherParameterName, sixthWeatherParameterValue,
			firstWeatherParameterDate, secondWeatherParameterDate, thirdWeatherParameterDate,
			fourthWeatherParameterDate,	fifthWeatherParameterDate, sixthWeatherParameterDate;

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
}