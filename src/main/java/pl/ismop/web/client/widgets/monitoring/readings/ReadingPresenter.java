package pl.ismop.web.client.widgets.monitoring.readings;

import java.util.List;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.monitoring.readings.IReadingsView.IReadingsPresenter;

@Presenter(view = ReadingsView.class)
public class ReadingPresenter extends BasePresenter<IReadingsView, MainEventBus> implements IReadingsPresenter {
	public void onShowExpandedReadings(List<ChartSeries> series) {
		view.showModal(true);
	}
}