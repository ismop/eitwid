package pl.ismop.web.client.widgets.slider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateEvent;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.events.ChangeDateHandler;
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

    public SliderView() {
        initWidget(uiBinder.createAndBindUi(this));

        slider.setFormatter(new FormatterCallback() {
            @Override
            public String formatTooltip(double v) {
                return presenter.getLabel(v);
            }
        });

        startDate.addChangeDateHandler(new ChangeDateHandler() {
            @Override
            public void onChangeDate(ChangeDateEvent changeDateEvent) {
                GWT.log("Start date changed: " + startDate.getValue());
                presenter.onStartDateChanged(startDate.getValue());
            }
        });

        endDate.addChangeDateHandler(new ChangeDateHandler() {
            @Override
            public void onChangeDate(ChangeDateEvent changeDateEvent) {
                presenter.onEndDateChanged(endDate.getValue());
            }
        });
    }

    @UiHandler("slider")
    void onSlideStop(SlideStopEvent<Double> event) {
        //for some reason going through String value was necessary
        String val = "" + event.getValue();
        getPresenter().onSliderChanged(Double.parseDouble(val));
    }

    @Override
    public void setStartDate(Date date) {
        startDate.setValue(date);
    }

    @Override
    public void setEndDate(Date date) {
        endDate.setValue(date);
    }

    @Override
    public void setNumberOfPoints(long numberOfPoints) {
        slider.setMax(numberOfPoints);
    }

    @Override
    public void setPresenter(ISliderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ISliderPresenter getPresenter() {
        return presenter;
    }
}
