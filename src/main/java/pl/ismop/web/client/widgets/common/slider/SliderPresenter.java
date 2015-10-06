package pl.ismop.web.client.widgets.common.slider;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;

import java.util.Date;

/**
 * Created by marek on 09.09.15.
 */
@Presenter(view = SliderView.class, multiple = true)
public class SliderPresenter extends BasePresenter<ISliderView, MainEventBus> implements ISliderView.ISliderPresenter {
    public static final int STEP = 900000; // 15 minutes
    public static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

    public static class Events {
        public void onStartDateChanged(Date startDate) {}
        public void onEndDateChanged(Date endDate) {}
        public void onDateChanged(Date selectedDate) {}
    }

    private Date endDate;
    private Date startDate;
    private Events eventsListener;

    public SliderPresenter() {
        // Initialize with NullObject
        setEventsListener(null);
    }

    @Override
    public void bind() {
        super.bind();
        initDates();
    }

    private void initDates() {
        endDate = new Date();
        startDate = new Date(endDate.getTime() - 14 * DAY_IN_MS);

        setStartDate(startDate);
        setEndDate(endDate);
    }

    public void setStartDate(Date date) {
        view.setStartDate(date);
        onStartDateChanged(date);
    }

    public void setEndDate(Date date) {
        view.setEndDate(date);
        onEndDateChanged(date);
    }

    public Date getSelectedDate() {
        return getDate(view.getSelectedPoint());
    }

    private long calculateNumberOfPoints() {
        long interval = endDate.getTime() - startDate.getTime();
        return interval / STEP;
    }

    @Override
    public void onSliderChanged(Double value) {
        eventsListener.onDateChanged(getDate(value));
    }

    @Override
    public void onStartDateChanged(Date startDate) {
        this.startDate = startDate;
        view.setNumberOfPoints(calculateNumberOfPoints());
        eventsListener.onStartDateChanged(startDate);
    }

    @Override
    public void onEndDateChanged(Date endDate) {
        this.endDate = endDate;
        view.setNumberOfPoints(calculateNumberOfPoints());
        eventsListener.onEndDateChanged(endDate);
    }

    private Date getDate(double point) {
        return new Date(startDate.getTime() + (long)(point * STEP));
    }

    @Override
    public String getLabel(double point) {
        return getDate(point).toString();
    }

    @Override
    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    @Override
    public Date getEndDate() {
        return new Date(endDate.getTime());
    }

    public void setAllowEditDateIntervals(boolean enabled) {
        getView().setAllowEditDateIntervals(enabled);
    }

    public void setEventsListener(Events eventsListener) {
        this.eventsListener = eventsListener;
        if(eventsListener == null) {
            // NullObject value to not check every time if eventsListener is null.
            this.eventsListener = new Events();
        }
    }
}
