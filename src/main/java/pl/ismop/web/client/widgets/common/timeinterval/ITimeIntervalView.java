package pl.ismop.web.client.widgets.common.timeinterval;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.Date;

public interface ITimeIntervalView extends IsWidget {
    interface ITimeIntervalPresenter {
        void onOk(Date from, Date to);
        void onCancel();
    }

    void show(Date from, Date to);
    void close();
}
