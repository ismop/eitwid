package pl.ismop.web.client.widgets.common.slider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateEvent;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.FormatterCallback;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;

import java.util.Date;

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

    public SliderView() {
        initWidget(uiBinder.createAndBindUi(this));

        slider.setFormatter(new FormatterCallback() {
            @Override
            public String formatTooltip(double v) {
                return presenter.getLabel(v);
            }
        });
    }

    @UiHandler("startDate")
    void onStartDateChanged(ChangeDateEvent event) {
        presenter.onStartDateChanged(startDate.getValue());
        presenter.onSliderChanged(slider.getValue());
    }

    @UiHandler("endDate")
    void onEndDateChanged(ChangeDateEvent event) {
        presenter.onEndDateChanged(endDate.getValue());
        presenter.onSliderChanged(slider.getValue());
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
    }

    @Override
    public Double getSelectedPoint() {
        return slider.getValue();
    }

    @Override
    public void setAllowEditDateIntervals(boolean enabled) {
        allowEditTimeIntervals = enabled;
        startDate.setEnabled(enabled);
        endDate.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled) {
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
	public void setDateFormat(String format) {
		startDate.setFormat(format);
		endDate.setFormat(format);
	}
}