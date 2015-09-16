package pl.ismop.web.client.widgets.monitoring.fibre;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.monitoring.fibre.IFibreView.IFibrePresenter;
import pl.ismop.web.client.widgets.slider.SliderPresenter;

@Presenter(view = FibreView.class)
public class FibrePresenter extends BasePresenter<IFibreView, MainEventBus> implements IFibrePresenter {
	private final DapController dapController;
	private Chart chart;
	private SliderPresenter slider;
	private MapPresenter map;

	private IDataFetcher fetcher;
	Map<String, Device> deviceMapping = new HashMap<>();
	Map<String, Series> seriesCache = new HashMap<>();
	private Levee levee;

	@Inject
	public FibrePresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void onShowFibrePanel(Levee levee) {
		if (this.levee != levee) {
			this.levee = levee;
			fetcher = new DataFetcher(dapController, levee);
		}

		initChart();
		initSlider();
		initializeFetcher();

		view.showModal(true);
	}

	@Override
	public void onModalReady() {
		initLeveeMinimap();
		chart.reflow();
	}

	private void initChart() {
		if(chart != null) {
			chart.removeAllSeries();
			seriesCache.clear();
		} else {
			chart = new Chart().
					setChartTitle(new ChartTitle()).
					setWidth100();

			chart.getYAxis().setAxisTitle(new AxisTitle().setText("Temperarura [\u00B0C]"));
			chart.getXAxis().setAxisTitle(new AxisTitle().setText("Metr bieżacy wału [m]"));

			chart.setSeriesPlotOptions(new SeriesPlotOptions().
							setPointMouseOverEventHandler(new PointMouseOverEventHandler() {
								private Section selectedSection;
								private Device selectedDevice;

								@Override
								public boolean onMouseOver(PointMouseOverEvent pointMouseOverEvent) {
									Device selectedDevice = deviceMapping.get(pointMouseOverEvent.getSeriesName() + "::" + pointMouseOverEvent.getXAsString());
									Section selectedSection = null;
									if (selectedDevice != null) {
										selectedSection = fetcher.getDeviceSection(selectedDevice);
										map.highlightSection(selectedSection, true);
										map.addDevice(selectedDevice);
									}
									if (this.selectedSection != null && this.selectedSection != selectedSection) {
										map.highlightSection(this.selectedSection, false);
									}

									if (this.selectedDevice != null && this.selectedDevice != selectedDevice) {
										map.removeDevice(this.selectedDevice);
									}
									this.selectedDevice = selectedDevice;
									this.selectedSection = selectedSection;
									return true;
								}
							})
			);

			chart.setOption("/chart/zoomType", "x");

			chart.setToolTip(new ToolTip()
							.setFormatter(new ToolTipFormatter() {
								public String format(ToolTipData toolTipData) {
									Device selectedDevice = deviceMapping.get(toolTipData.getSeriesName() + "::" + toolTipData.getXAsString());
									if (selectedDevice != null) {
										return "<b>" + toolTipData.getYAsString() + "\u00B0C</b><br/>" +
												toolTipData.getXAsString() + " metr wału<br/>" +
												toolTipData.getXAsString() + " metr światłowodu<br/>" +
												"Sensor: " + selectedDevice.getId();
									} else {
										return null;
									}
								}
							})
			);

			view.addElementToLeftPanel(chart);
		}
	}

	private void initializeFetcher() {
		chart.showLoading("Getting fibre shape from DAP");
		fetcher.initialize(new IDataFetcher.InitializeCallback() {
			@Override
			public void ready() {
				chart.removeAllSeries();
				chart.hideLoading();
				loadData(slider.getSelectedDate());
				for (Section section : fetcher.getSections()) {
					map.addSection(section);
				}
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				chart.showLoading("Unable to get fibre shape from DAP");
			}
		});
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
			view.addElementToLeftPanel(slider.getView());
		}
	}

	private void initLeveeMinimap() {
		if (map == null) {
			map = eventBus.addHandler(MapPresenter.class);
			view.addElementToRightPanel(map.getView());
		}
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
				deviceMapping.clear();
				Map<String, Series> newSeriesCache = new HashMap<>();
				for (Map.Entry<DeviceAggregation, List<IDataFetcher.ChartPoint>> points : series.entrySet()) {
					DeviceAggregation aggregation = points.getKey();
					Series s = getSeries(aggregation);
					newSeriesCache.put(aggregation.getId(), s);
					upateSeries(s, points);
				}
				for (Series s : seriesCache.values()) {
					chart.removeSeries(s, false);
				}
				seriesCache = newSeriesCache;
				chart.hideLoading();
				chart.redraw();
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				chart.showLoading("Loading data from DAP failed");
			}
		});
	}

	private Series getSeries(DeviceAggregation aggregation) {
		Series s = seriesCache.remove(aggregation.getId());
		if (s == null) {
			s = chart.createSeries().
					setName(aggregation.getId()).
					setType(Type.SPLINE);
		}

		return s;
	}

	private void upateSeries(Series s, Map.Entry<DeviceAggregation, List<IDataFetcher.ChartPoint>> points) {
		DeviceAggregation aggregation = points.getKey();
		List<IDataFetcher.ChartPoint> newPoints = points.getValue();

		if (theSameX(s.getPoints(), newPoints)) {
			Point[] seriesPoints = s.getPoints();
			for (int i = 0; i < newPoints.size(); i++) {
				Point seriesPoint = seriesPoints[i];
				IDataFetcher.ChartPoint newPoint = newPoints.get(i);
				seriesPoint.update(newPoint.getX(), newPoint.getY(), false);
				deviceMapping.put(aggregation.getId() + "::" + newPoint.getX(), newPoint.getDevice());
			}
		} else {
			s.remove();
			for (IDataFetcher.ChartPoint point : newPoints) {
				s.addPoint(point.getX(), point.getY());
				deviceMapping.put(aggregation.getId() + "::" + point.getX(), point.getDevice());
			}
			chart.addSeries(s, false, true);
		}
	}

	private boolean theSameX(Point[] points, List<IDataFetcher.ChartPoint> newPoints) {
		return points !=null && newPoints != null &&
				points.length == newPoints.size();
	}
}