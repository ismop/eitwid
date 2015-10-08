package pl.ismop.web.client.widgets.analysis.chart;

import com.google.gwt.core.client.GWT;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.widgets.analysis.chart.wizard.ChartWizardPresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

import java.util.Date;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus>
        implements IPanelContent<IChartView, MainEventBus> {
    private Experiment selectedExperiment;

    @Override
    public void setSelectedExperiment(Experiment experiment) {
        selectedExperiment = experiment;
    }

    @Override
    public void setSelectedDate(Date date) {

    }

    @Override
    public void edit() {
        eventBus.addHandler(ChartWizardPresenter.class).show(selectedExperiment, new ChartWizardPresenter.ShowResult() {
            @Override
            public void ok() {
                GWT.log("Update tab configuration");
            }
        });
    }

    @Override
    public void setSelectionManager(ISelectionManager selectionManager) {

    }
}
