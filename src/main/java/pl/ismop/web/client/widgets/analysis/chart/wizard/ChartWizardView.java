package pl.ismop.web.client.widgets.analysis.chart.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.shared.event.ModalHiddenEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Modal;
import pl.ismop.web.client.widgets.analysis.chart.wizard.IChartWizardView.IChartWizardPresenter;
import pl.ismop.web.client.widgets.common.map.IMapView;

public class ChartWizardView extends Composite implements IChartWizardView, ReverseViewInterface<IChartWizardPresenter>{
    interface ChartWizardUiBinder extends UiBinder<Widget, ChartWizardView> {
    }

    private static ChartWizardUiBinder uiBinder = GWT.create(ChartWizardUiBinder.class);

    private IChartWizardPresenter presenter;

    @UiField
    Modal modal;

    @UiField
    FlowPanel mapPanel;

    @UiField
    FlowPanel panels;

    @UiHandler("modal")
    void onModalHiden(ModalHiddenEvent even) {
        getPresenter().modalCanceled();
    }

    @UiHandler("modal")
    void onPopupReady(ModalShownEvent event) {
        getPresenter().onModalReady();
    }

    @UiHandler("okButton")
    void onOk(ClickEvent event) {
        modal.hide();
        getPresenter().modalOk();
    }

    @UiHandler("addSensorButton")
    void onAddSensor(ClickEvent event) {
        getPresenter().addSensor();
    }

    public ChartWizardView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void show() {
        modal.show();
    }

    @Override
    public void setMiniMap(IMapView minimap) {
//        mapPanel.clear();
        mapPanel.add(minimap);
    }

    @Override
    public void addSensorPanel(IsWidget panel) {
        panels.add(panel);
    }

    @Override
    public void setPresenter(IChartWizardPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public IChartWizardPresenter getPresenter() {
        return presenter;
    }
}
