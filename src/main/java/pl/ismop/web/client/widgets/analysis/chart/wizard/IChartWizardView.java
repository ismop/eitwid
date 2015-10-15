package pl.ismop.web.client.widgets.analysis.chart.wizard;

import com.google.gwt.user.client.ui.IsWidget;
import pl.ismop.web.client.widgets.common.map.IMapView;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.common.map.MapView;
import pl.ismop.web.client.widgets.common.panel.IPanelView;

import java.util.Collection;
import java.util.List;

public interface IChartWizardView extends IsWidget {
    void show();
    void removeFromParent();
    void setMiniMap(IMapView minimap);

    void addPanel(IsWidget panel);

    void setDevices(Collection<String> names);

    ChartWizardMessages getMessages();

    void setLoading(String text);

    void removePanel(IsWidget panel);

    void unselectParameter(String parameterName);

    void setOkEnabled(boolean enabled);

    interface IChartWizardPresenter {
        void onModalReady();

        void modalCanceled();

        void modalOk();

        void addParameter(String parameterName);

        void removeParameter(String parameterName);
    }
}
