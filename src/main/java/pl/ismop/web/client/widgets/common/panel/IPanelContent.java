package pl.ismop.web.client.widgets.common.panel;

import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.PresenterInterface;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

import java.util.Date;

public interface IPanelContent<V, E extends EventBus> extends PresenterInterface<V, E> {
    void setSelectedExperiment(Experiment experiment);
    void setSelectedDate(Date date);
    void edit();
    void setSelectionManager(ISelectionManager selectionManager);
}
