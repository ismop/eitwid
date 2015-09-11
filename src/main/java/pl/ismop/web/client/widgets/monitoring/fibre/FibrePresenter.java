package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import org.gwtbootstrap3.client.ui.Label;
import org.moxieapps.gwt.highcharts.client.*;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.events.*;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.monitoring.fibre.IFibreView.IFibrePresenter;
import pl.ismop.web.client.widgets.slider.SliderPresenter;

import java.util.*;

@Presenter(view = FibreView.class)
public class FibrePresenter extends BasePresenter<IFibreView, MainEventBus> implements IFibrePresenter {
	private Chart chart;
	private SliderPresenter slider;
	private Label status;

	private IDataFetcher fetcher;

	public FibrePresenter() {
		fetcher = new MockDateFetcher();
	}

	public void onShowFibrePanel() {
		view.showModal(true);

		initSlider();
		initChart();
		initLeveeMinimap();
	}

	private void initChart() {
		if(chart != null) {
			chart.removeAllSeries();
			chart.removeFromParent();
		}

		chart = new Chart().
				setChartTitle(new ChartTitle()).
				setWidth(1100);
//		chart.getXAxis().setPlotBands(createPlotBands(chart.getXAxis()));
		chart.getYAxis().setAxisTitle(new AxisTitle().setText("Temperarura [\u00B0C]"));
		chart.getXAxis().setAxisTitle(new AxisTitle().setText("Metr bieżacy wału [m]"));

		chart.setSeriesPlotOptions(new SeriesPlotOptions().
				setPointClickEventHandler(new PointClickEventHandler() {
					@Override
					public boolean onClick(PointClickEvent pointClickEvent) {
						GWT.log(pointClickEvent.getSeriesName() + " " + pointClickEvent.getXAsString() + ":" + pointClickEvent.getYAsString());
						return true;
					}
				}).
				setPointMouseOverEventHandler(new PointMouseOverEventHandler() {
					@Override
					public boolean onMouseOver(PointMouseOverEvent pointMouseOverEvent) {
						GWT.log("over: " + pointMouseOverEvent.getSeriesName() + " " + pointMouseOverEvent.getXAsString() + ":" + pointMouseOverEvent.getYAsString());
						return true;
					}
				})
		);

		loadData(slider.getSelectedDate());

		view.setChart(chart);
	}

	private void initSlider() {
		if (slider == null) {
			slider = eventBus.addHandler(SliderPresenter.class);
			slider.setEventsListener(new SliderPresenter.Events() {
				@Override
				public void onDateChanged(Date selectedDate) {
					onSliderChanged(selectedDate);
				}
			});
			view.setSlider(slider.getView());
		}
	}

	private void initLeveeMinimap() {
		if (status == null) {
			status = new Label();
			view.setEmbenkment(status);
		}

		status.setText("Testing string");
	}

	private PlotBand[] createPlotBands(XAxis axis) {
		int[] points = new int[] {0, 100, 110, 120, 120, 130, 130, 150, 150, 160, 160, 210, 220, 320, 330, 340, 340, 350, 350, 370, 370, 380, 380, 430};
		PlotBand[] result = new PlotBand[(int) (points.length / 2)];
		Random random = new Random();
		
		for(int i = 0; i < points.length; i = i + 2) {
			result[i / 2] = axis.createPlotBand().setFrom(points[i]).setTo(points[i + 1]).setColor("#" + random.nextInt(10) + "50" + random.nextInt(10) + random.nextInt(10) + "c");
		}
		
		return result;
	}

	@Override
	public void onSliderChanged(final Date selectedDate) {
		loadData(selectedDate);
	}

	private void loadData(Date selectedDate) {
		chart.showLoading("Loading data from DAP");
		fetcher.getSeries(selectedDate, new IDataFetcher.SeriesCallback() {
			@Override
			public void series(Map<DeviceAggregation, List<IDataFetcher.ChartPoint>> series) {
				chart.removeAllSeries();
				for(Map.Entry<DeviceAggregation, List<IDataFetcher.ChartPoint>> points : series.entrySet()) {
					Series s = chart.createSeries()
							.setName(points.getKey().getId())
							.setType(Type.SPLINE);
					for (IDataFetcher.ChartPoint point : points.getValue()) {
						s.addPoint(point.getX(), point.getY());
					}

					chart.addSeries(s);
					chart.hideLoading();
				}
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				chart.showLoading("Loading data from DAP failed");
			}
		});
	}
}