package pl.ismop.web.client.widgets.analysis.comparison;

import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.PresenterInterface;
import pl.ismop.web.client.dap.experiment.Experiment;

import java.util.Date;

public interface IPanelContent<V, E extends EventBus> extends PresenterInterface<V, E> {
    void setSelectedExperiment(Experiment experiment);
    void setSelectedDate(Date date);
}
