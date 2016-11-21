package pl.ismop.web.client.widgets.analysis.chart.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.shared.event.ModalHiddenEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

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

    @UiField
    Row loading;

    @UiField
    InlineLabel loadingLabel;

    @UiField
    ChartWizardMessages messages;

    @UiField
    MultipleSelect devicesSelect;

    @UiField
    Row devices;

    @UiField
    Button okButton;

    @UiField
    CheckBox changeTrends;

    private List<String> selected = new ArrayList<>();

    private Option defaultOption;

    public ChartWizardView() {
        initWidget(uiBinder.createAndBindUi(this));
        okButton.setEnabled(false);
        devicesSelect.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> changeEvent) {
                List<String> toUnselect = new ArrayList<>(selected);
                toUnselect.removeAll(devicesSelect.getValue());

                List<String> toSelect = new ArrayList<>(devicesSelect.getValue());
                toSelect.removeAll(selected);

                selected = new ArrayList<>(devicesSelect.getValue());

                for (String s : toSelect) {
                    getPresenter().addParameter(s);
                }

                for (String s : toUnselect) {
                    getPresenter().removeParameter(s);
                }
            }
        });
    }

    @Override
    public void show() {
        modal.show();
    }

    @Override
    public void setMiniMap(IMapView minimap) {
        mapPanel.add(minimap);
    }

    @Override
    public void setPresenter(IChartWizardPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public IChartWizardPresenter getPresenter() {
        return presenter;
    }

    @UiHandler("modal")
    void onModalHiden(ModalHiddenEvent even) {
        getPresenter().modalCanceled();
    }

    @UiHandler("modal")
    void onPopupReady(ModalShownEvent event) {
        getPresenter().onModalReady();
    }

    @UiHandler("changeTrends")
    void onChangeTrendsClicked(ClickEvent click) {
        getPresenter().setChangeTrends(changeTrends.getValue());
    }

    @UiHandler("okButton")
    void onOk(ClickEvent event) {
        modal.hide();
        getPresenter().modalOk();
    }

    @Override
    public void setDevices(Collection<String> names) {
        loading.setVisible(false);
        devices.setVisible(true);

        devicesSelect.clear();

        List<String> sortedNames = new ArrayList<>(names);
        Collections.sort(sortedNames);

        for(String name : sortedNames) {
            Option o = new Option();
            o.setText(name);
            devicesSelect.add(o);
        }
        devicesSelect.refresh();
    }

    @Override
    public ChartWizardMessages getMessages() {
        return messages;
    }

    @Override
    public void setLoading(String text) {
        loadingLabel.setText(text);

        devices.setVisible(false);
        loading.setVisible(true);
    }

    @Override
    public void addPanel(IsWidget panel) {
        panels.add(panel);
    }

    @Override
    public void removePanel(IsWidget panel) {
        panels.remove(panel);
    }

    @Override
    public void unselectParameter(String parameterName) {
        selected.remove(parameterName);
        devicesSelect.setValue(selected);
    }

    @Override
    public void setOkEnabled(boolean enabled) {
        okButton.setEnabled(enabled);
    }

    @Override
    public void close() {
        modal.setVisible(false);
    }
}
