package pl.ismop.web.client.widgets.monitoring.waterheight;

import java.util.stream.Stream;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.util.TimelineZoomDataCallbackHelper;
import pl.ismop.web.client.util.WaterHeight;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.monitoring.waterheight.IWaterHeightView.IWaterHeightPresenter;

@Presenter(view = WaterHeightView.class)
public class WaterHeightPresenter extends BasePresenter<IWaterHeightView, MainEventBus>
		implements IWaterHeightPresenter {

	private DapController dapController;
	private ChartPresenter waterHeightChart;

	// use Level2_PV sensor

	@Inject
	public WaterHeightPresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void onShowWaterHightPanel(Levee selectedLevee) {
		view.showModal(true);
	}

	@Override
	public void onModalReady() {
		initChart();
		loadWaterHeight();
	}

	private void initChart() {
		if (waterHeightChart != null) {

		} else {
			waterHeightChart = eventBus.addHandler(ChartPresenter.class);
			waterHeightChart.setHeight(view.getChartHeight());
			view.setChart(waterHeightChart.getView());
			waterHeightChart.initChart();
			waterHeightChart
					.setZoomDataCallback(new TimelineZoomDataCallbackHelper(dapController, eventBus, waterHeightChart));
		}
	}

	private void loadWaterHeight() {
		waterHeightChart.setLoadingState(true);
		new WaterHeight(dapController).loadAverage(new WaterHeight.WaterHeightCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				waterHeightChart.setLoadingState(false);
				eventBus.showError(errorDetails);
			}

			@Override
			public void success(Stream<ChartSeries> series) {
				series.forEach(chartSeries -> waterHeightChart.addChartSeries(chartSeries));
				waterHeightChart.setLoadingState(false);
			}
		});
	}
}
