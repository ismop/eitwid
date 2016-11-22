package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISensorPanelView extends IsWidget {
    void setHeaderTitle(String title);

    SensorPanelMessages getMessages();

    void setLoading(String message);

    void setTimelines(Collection<String> selected, Collection<String> notSelected);

    List<String> getSelected();

    interface ISensorPanelPresenter {
        void timelineSelectionChanged();
    }
}
