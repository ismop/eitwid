package pl.ismop.web.client.widgets.monitoring.weather;

import org.gwtbootstrap3.client.shared.event.ModalHiddenEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Span;
import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class WeatherStationView extends Composite implements IWeatherStationView, ReverseViewInterface<IWeatherStationView.IWeatherStationPresenter> {
	private static WeatherStationViewUiBinder uiBinder = GWT.create(WeatherStationViewUiBinder.class);

	interface WeatherStationViewUiBinder extends UiBinder<Widget, WeatherStationView> {}
	
	interface WeatherStationViewStyle extends CssResource {
		String parameterLabel();
	}
	
	@UiField WeatherStationViewMessages messages;
	@UiField Modal modal;
	@UiField FlowPanel progress;
	@UiField Heading weatherHeading1;
	@UiField Heading weatherHeading2;
	@UiField FlowPanel chartSlot;
	@UiField FlexTable measurements1;
	@UiField FlexTable measurements2;
	@UiField Container container;
	
	@UiField WeatherStationViewStyle style;

	private IWeatherStationPresenter presenter;

	private int activeCheckboxCounter = 0; 
	
	private CheckBox initialCheckbox = null;
	
	public WeatherStationView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showModal() {
		modal.show();
	}

	@UiHandler("modal")
	void modalShown(ModalShownEvent event) {
		getPresenter().onModalShown();
	}
	
	@UiHandler("modal")
	void modalHidden(ModalHiddenEvent event) {
		getPresenter().onModalHidden();
	}
	
	@Override
	public void showProgress(boolean show) {
		progress.setVisible(show);
	}

	@Override
	public void setChart(Chart firstChart) {
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
	public void clearMeasurements() {
		measurements1.removeAllRows();
		measurements2.removeAllRows();
		initialCheckbox = null; 
		activeCheckboxCounter = 0;
	}
	
	@Override
	public void addLatestReading1(String parameterId, String parameterName,
			String typeName, String value, String unit, String timestamp) {
		addReadingsToTable(measurements1, parameterId, parameterName, typeName,
				value, unit, timestamp);
	}

	@Override
	public void addLatestReading2(String parameterId, String parameterName,
			String typeName, String value, String unit, String timestamp) {
		addReadingsToTable(measurements2, parameterId, parameterName, typeName,
				value, unit, timestamp);
	}
	
	private Widget addReadingsToTable(FlexTable measurements,
			final String parameterId, String parameterName, String typeName,
			String value, String unit, String timestamp) {
		
		int row = measurements.getRowCount();
		
		final CheckBox checkBox = new CheckBox();
		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue()) {
					activeCheckboxCounter++;
				} else {
					activeCheckboxCounter--;
				}
				// one checkbox has to be checked - always
				if (activeCheckboxCounter>0) {
					presenter.loadParameter(parameterId, event.getValue());
				} else {
					activeCheckboxCounter++;
					checkBox.setValue(true, false);
				}
			}
		});
		
		measurements.setWidget(row, 0, checkBox);
		
		Span l = new Span(typeName);
		l.addStyleName(style.parameterLabel());
		l.getElement().setAttribute("title", parameterName);
		measurements.setWidget(row, 1, l);
		measurements.setText(row, 2, value + " " + unit);
		measurements.setText(row, 3, timestamp);

		// when the view is populated with parameters first checkbox is checked 
		// - setting value to true fires event in order to trigger chart generation 
		if (initialCheckbox == null) {
			initialCheckbox = checkBox;
			initialCheckbox.setValue(true, true);
		}
		
		return measurements;
	}

	@Override
	public int getChartContainerHeight() {
		return chartSlot.getOffsetHeight();
	}
	
	@Override
	public void setChart(IsWidget chart) {
		chartSlot.add(chart);
	}

	@Override
	public void setPresenter(IWeatherStationPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IWeatherStationPresenter getPresenter() {
		return presenter;
	}

	@Override
	public String getNoReadingLabel() {
		return messages.noReadingLabel();
	}
}