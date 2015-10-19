package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import com.google.gwt.user.client.ui.IsWidget;

import java.util.Collection;
import java.util.List;

public interface ISensorPanelView extends IsWidget {
    void setHeaderTitle(String title);

    SensorPanelMessages getMessages();

    void setLoading(String message);

    void setTimelines(Collection<String> timelines);

    List<String> getSelected();

    interface ISensorPanelPresenter {
        void timelineSelectionChanged();
    }
}
