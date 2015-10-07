package pl.ismop.web.client.widgets.analysis.chart.wizard;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.widgets.analysis.chart.wizard.IChartWizardView.IChartWizardPresenter;

@Presenter(view = ChartWizardView.class, multiple = true)
public class ChartWizardPresenter extends BasePresenter<IChartWizardView, MainEventBus> implements IChartWizardPresenter {
    private ShowResult showResult;

    public interface ShowResult {
        void ok();
    }

    private final DapController dapController;

    @Inject
    public ChartWizardPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    public void show(ShowResult showResult) {
        this.showResult = showResult;
        getView().show();
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

    private void destroy() {
        eventBus.removeHandler(this);
        getView().removeFromParent();
    }
}
