package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.gwtbootstrap3.extras.select.client.ui.Select;

public class SensorPanelView extends Composite implements ISensorPanelView {
    interface SensorPanelViewUiBinder extends UiBinder<Widget, SensorPanelView> {
    }

    private static SensorPanelViewUiBinder uiBinder = GWT.create(SensorPanelViewUiBinder.class);

    @UiField
    Panel panel;

    @UiField
    Button closeButton;

    @UiField
    Select devices;

    public SensorPanelView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("panelWrapper")
    void onMouseOver(MouseOverEvent over) {
        setPanelType(PanelType.INFO, ButtonType.INFO);
    }

    @UiHandler("panelWrapper")
    void onMouseOut(MouseOutEvent out) {
        setPanelType(PanelType.DEFAULT, ButtonType.DEFAULT);
    }

    void setPanelType(PanelType panelType, ButtonType buttonType) {
        panel.setType(panelType);
        closeButton.setType(buttonType);
        devices.setStyle(buttonType);
    }
}
