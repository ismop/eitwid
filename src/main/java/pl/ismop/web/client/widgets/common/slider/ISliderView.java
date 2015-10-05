package pl.ismop.web.client.widgets.common.slider;


import com.google.gwt.user.client.ui.IsWidget;

import java.util.Date;

public interface ISliderView extends IsWidget {
    interface ISliderPresenter {
        void onSliderChanged(Double value);
        void onStartDateChanged(Date date);
        void onEndDateChanged(Date date);
        String getLabel(double point);
        Date getStartDate();
        Date getEndDate();
    }

    void setStartDate(Date date);
    void setEndDate(Date date);
    void setNumberOfPoints(long numberOfPoints);
    Double getSelectedPoint();
    void setAllowEditDateIntervals(boolean enabled);
}

