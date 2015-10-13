package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel.ISensorPanelView.ISensorPanelPresenter;

@Presenter(view = SensorPanelView.class, multiple = true)
public class SensorPanelPresenter extends BasePresenter<ISensorPanelView, MainEventBus>
        implements ISensorPanelPresenter {
    private Parameter parameter;

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
        getView().setHeaderTitle(parameter.getDevice().getCustomId() + " (" + parameter.getMeasurementTypeName() + ")");
    }

    public Parameter getParameter() {
        return parameter;
    }
}
