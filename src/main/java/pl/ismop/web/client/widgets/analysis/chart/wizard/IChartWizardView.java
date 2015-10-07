package pl.ismop.web.client.widgets.analysis.chart.wizard;

public interface IChartWizardView {
    void show();
    void removeFromParent();

    interface IChartWizardPresenter {
        void modalCanceled();

        void modalOk();
    }
}
