package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel.ISensorPanelView.ISensorPanelPresenter;

public class SensorPanelView extends Composite implements ISensorPanelView, ReverseViewInterface<ISensorPanelPresenter> {
    private ISensorPanelPresenter presenter;

    interface SensorPanelViewUiBinder extends UiBinder<Widget, SensorPanelView> {
    }

    private static SensorPanelViewUiBinder uiBinder = GWT.create(SensorPanelViewUiBinder.class);

    @UiField
    Panel panel;

    @UiField
    Heading heading;

    public SensorPanelView() {
        initWidget(uiBinder.createAndBindUi(this));
        heading.setText("Device name (parameter type)");
    }

    @UiHandler("panelWrapper")
    void onMouseOver(MouseOverEvent over) {
        setPanelType(PanelType.INFO);
    }

    @UiHandler("panelWrapper")
    void onMouseOut(MouseOutEvent out) {
        setPanelType(PanelType.DEFAULT);
    }

    void setPanelType(PanelType panelType) {
        panel.setType(panelType);
    }

    @Override
    public void setHeaderTitle(String title) {
        heading.setText(title);
    }

    @Override
    public void setPresenter(ISensorPanelPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ISensorPanelPresenter getPresenter() {
        return presenter;
    }
}
