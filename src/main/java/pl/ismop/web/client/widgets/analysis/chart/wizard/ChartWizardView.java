package pl.ismop.web.client.widgets.analysis.chart.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.shared.event.ModalHiddenEvent;
import org.gwtbootstrap3.client.ui.Modal;
import pl.ismop.web.client.widgets.analysis.chart.wizard.IChartWizardView.IChartWizardPresenter;

public class ChartWizardView extends Composite implements IChartWizardView, ReverseViewInterface<IChartWizardPresenter>{
    interface ChartWizardUiBinder extends UiBinder<Widget, ChartWizardView> {
    }

    private static ChartWizardUiBinder uiBinder = GWT.create(ChartWizardUiBinder.class);

    private IChartWizardPresenter presenter;

    @UiField
    Modal modal;

    @UiHandler("modal")
    void onModalHiden(ModalHiddenEvent even) {
        getPresenter().modalCanceled();
    }

    @UiHandler("okButton")
    void onOk(ClickEvent event) {
        modal.hide();
        getPresenter().modalOk();
    }

    public ChartWizardView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void show() {
        modal.show();
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
