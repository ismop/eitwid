package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel.ISensorPanelView.ISensorPanelPresenter;

import java.util.Collection;
import java.util.List;

public class SensorPanelView extends Composite implements ISensorPanelView, ReverseViewInterface<ISensorPanelPresenter> {
    private ISensorPanelPresenter presenter;

    interface SensorPanelViewUiBinder extends UiBinder<Widget, SensorPanelView> {
    }

    private static SensorPanelViewUiBinder uiBinder = GWT.create(SensorPanelViewUiBinder.class);

    @UiField
    Panel panel;

    @UiField
    Heading heading;

    @UiField
    SensorPanelMessages messages;

    @UiField
    Row loading;

    @UiField
    InlineLabel loadingLabel;

    @UiField
    Row timelines;

    @UiField
    Select timelinesSelect;

    public SensorPanelView() {
        initWidget(uiBinder.createAndBindUi(this));

        timelinesSelect.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                getPresenter().timelineSelectionChanged();
            }
        });
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

    @Override
    public SensorPanelMessages getMessages() {
        return messages;
    }

    @Override
    public void setLoading(String message) {
        loadingLabel.setText(" " + message);
        loading.setVisible(true);
        timelines.setVisible(false);
    }

    @Override
    public void setTimelines(Collection<String> timelines) {
        for (String timeline : timelines) {
            Option o = new Option();
            o.setText(timeline);
            timelinesSelect.add(o);
        }
        timelinesSelect.refresh();

        loading.setVisible(false);
        this.timelines.setVisible(true);
    }

    @Override
    public List<String> getSelected() {
        return timelinesSelect.getAllSelectedValues();
    }
}
