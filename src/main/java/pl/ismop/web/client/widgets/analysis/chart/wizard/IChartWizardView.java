package pl.ismop.web.client.widgets.analysis.chart.wizard;

import com.google.gwt.user.client.ui.IsWidget;
import pl.ismop.web.client.widgets.common.map.IMapView;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.common.map.MapView;
import pl.ismop.web.client.widgets.common.panel.IPanelView;

public interface IChartWizardView {
    void show();
    void removeFromParent();
    void setMiniMap(IMapView minimap);

    void addSensorPanel(IsWidget panel);

    interface IChartWizardPresenter {
        void onModalReady();

        void modalCanceled();

        void modalOk();

        void addSensor();
    }
}
