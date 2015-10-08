package pl.ismop.web.client.widgets.analysis.chart.wizard;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.widgets.analysis.chart.wizard.IChartWizardView.IChartWizardPresenter;
import pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel.SensorPanelPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.common.panel.PanelPresenter;

@Presenter(view = ChartWizardView.class, multiple = true)
public class ChartWizardPresenter extends BasePresenter<IChartWizardView, MainEventBus> implements IChartWizardPresenter {
    private Experiment selectedExperiment;
    private ShowResult showResult;
    private MapPresenter miniMap;

    public interface ShowResult {
        void ok();
    }

    private final DapController dapController;

    @Inject
    public ChartWizardPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    public void show(Experiment selectedExperiment, ShowResult showResult) {
        this.selectedExperiment = selectedExperiment;
        this.showResult = showResult;
        getView().show();
    }

    @Override
    public void onModalReady() {
        miniMap = eventBus.addHandler(MapPresenter.class);
        view.setMiniMap(miniMap.getView());
        for(Section section : selectedExperiment.getSections()) {
            GWT.log(section.getId());
            miniMap.addSection(section);
        }
    }

    @Override
    public void modalCanceled() {
        destroy();
    }

    @Override
    public void modalOk() {
        showResult.ok();
        destroy();
    }

    @Override
    public void addSensor() {
        SensorPanelPresenter panelPresenter = eventBus.addHandler(SensorPanelPresenter.class);

        getView().addSensorPanel(panelPresenter.getView());
    }

    private void destroy() {
        eventBus.removeHandler(this);
        getView().removeFromParent();
    }
}
