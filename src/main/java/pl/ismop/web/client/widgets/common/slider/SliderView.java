package pl.ismop.web.client.widgets.common.slider;

import java.util.Date;

import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.constants.DateTimePickerLanguage;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateEvent;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.FormatterCallback;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

/**
 * Created by marek on 09.09.15.
 */
public class SliderView extends Composite implements ISliderView, ReverseViewInterface<ISliderView.ISliderPresenter> {
    private static SliderViewUiBinder uiBinder = GWT.create(SliderViewUiBinder.class);

    interface SliderViewUiBinder extends UiBinder<Widget, SliderView> {}

    private ISliderPresenter presenter;

    @UiField
    DateTimePicker startDate;

    @UiField
    DateTimePicker endDate;

    @UiField
    Slider slider;

    private boolean allowEditTimeIntervals = true;

	private boolean enabled;

    public SliderView() {
        initWidget(uiBinder.createAndBindUi(this));

        slider.setFormatter(new FormatterCallback<Double>() {
            @Override
            public String formatTooltip(Double v) {
                return presenter.getLabel(v);
            }
        });
    }

    @UiHandler("startDate")
    void onStartDateChanged(ChangeDateEvent event) {
    	//DateTimePicker component behaves weird when parsing dates hence the fox below
    	Date date = new Date(getTime(event.getNativeEvent())
    			+ new Date().getTimezoneOffset() * 60 * 1000);
        presenter.onStartDateChanged(date);
        presenter.onSliderChanged(slider.getValue());
    	Scheduler.get().scheduleDeferred(() -> startDate.setValue(date, false));
    }

    @UiHandler("endDate")
    void onEndDateChanged(ChangeDateEvent event) {
    	//DateTimePicker component behaves weird when parsing dates hence the fox below
    	Date date = new Date(getTime(event.getNativeEvent())
    			+ new Date().getTimezoneOffset() * 60 * 1000);
		presenter.onEndDateChanged(date);
        presenter.onSliderChanged(slider.getValue());
        Scheduler.get().scheduleDeferred(() -> endDate.setValue(date, false));
    }

    @UiHandler("slider")
    void onSlideStop(SlideStopEvent<Double> event) {
        //for some reason going through String value was necessary
        String val = "" + event.getValue();
        presenter.onSliderChanged(Double.parseDouble(val));
    }

    @Override
    public void setEalierDate(Date date) {
        startDate.setStartDate(date);
    }

    @Override
    public void setStartDate(Date date) {
        startDate.setValue(date);
        endDate.setStartDate(date);
    }

    @Override
    public void setEndDate(Date date) {
        endDate.setValue(date);
        startDate.setEndDate(date);
    }

    @Override
    public void setNumberOfPoints(long numberOfPoints) {
        slider.setMax(numberOfPoints);
        slider.setEnabled(enabled);
    }

    @Override
    public Double getSelectedPoint() {
        return slider.getValue();
    }

    @Override
    public void setSelectedPoint(Double value) {
        slider.setValue(value);
    }

    @Override
    public void setAllowEditDateIntervals(boolean enabled) {
        allowEditTimeIntervals = enabled;
        startDate.setEnabled(enabled);
        endDate.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled) {
    	this.enabled = enabled;
        slider.setEnabled(enabled);
        setAllowEditDateIntervals(allowEditTimeIntervals && enabled);
    }

    @Override
    public void setPresenter(ISliderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ISliderPresenter getPresenter() {
        return presenter;
    }

	@Override
	public void setDateFormatAndLanguage(String format, String localeValue) {
		startDate.setLanguage(DateTimePickerLanguage.valueOf(localeValue.toUpperCase()));
		startDate.setGWTFormat(format);
		endDate.setLanguage(DateTimePickerLanguage.valueOf(localeValue.toUpperCase()));
		endDate.setGWTFormat(format);
	}

	private native int getTime(Event event) /*-{
		return event.date.getTime();
	}-*/;
}