package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;

@Presenter(view = SensorPanelView.class, multiple = true)
public class SensorPanelPresenter extends BasePresenter<ISensorPanelView, MainEventBus> {
}
