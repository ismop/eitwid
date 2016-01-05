package pl.ismop.web.client.widgets.monitoring.fibre;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.json.client.JSONObject;
import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.DateTimeLabelFormats;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.PointEvent;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOutEvent;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOutEventHandler;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEventHandler;
import org.moxieapps.gwt.highcharts.client.events.PointSelectEvent;
import org.moxieapps.gwt.highcharts.client.events.PointSelectEventHandler;
import org.moxieapps.gwt.highcharts.client.events.PointUnselectEvent;
import org.moxieapps.gwt.highcharts.client.events.PointUnselectEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopProperties;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.DateChartPoint;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.common.slider.SliderPresenter;
import pl.ismop.web.client.widgets.monitoring.fibre.IDataFetcher.ChartPoint;
import pl.ismop.web.client.widgets.monitoring.fibre.IDataFetcher.DateSeriesCallback;
import pl.ismop.web.client.widgets.monitoring.fibre.IDataFetcher.DevicesDateSeriesCallback;
import pl.ismop.web.client.widgets.monitoring.fibre.IDataFetcher.InitializeCallback;
import pl.ismop.web.client.widgets.monitoring.fibre.IDataFetcher.SeriesCallback;
import pl.ismop.web.client.widgets.monitoring.fibre.IFibreView.IFibrePresenter;

@Presenter(view = FibreView.class)
public class FibrePresenter extends BasePresenter<IFibreView, MainEventBus> implements IFibrePresenter {
	private class DeviceData {
		private PlotLine plotLine;
		private Series series;

		public DeviceData(PlotLine plotLine) {
			this.plotLine = plotLine;
		}

		public PlotLine getPlotLine() {
			return plotLine;
		}

		public Series getSeries() {
			return series;
		}

		public void setSeries(Series series) {
			this.series = series;
		}
	}

	private final DapController dapController;
	private final IsmopProperties properties;
	private Chart fibreChart;
	private Chart deviceChart;
	private SliderPresenter slider;
	private MapPresenter map;

	private DataFetcher fetcher;
	Map<String, Device> deviceMapping = new HashMap<>();
	Map<String, Series> seriesCache = new HashMap<>();
	private Levee levee;
	private Map<Device, DeviceData> selectedDevices = new HashMap<>();

	private FibreMessages messages;

	@Inject
	public FibrePresenter(DapController dapController, IsmopProperties properties) {
		this.dapController = dapController;
		this.properties = properties;
	}

	@SuppressWarnings("unused")
	public void onShowFibrePanel(Levee levee) {
		if (this.levee != levee) {
			this.levee = levee;
			fetcher = new DataFetcher(dapController, levee);
			fetcher.setMock(false);
		}
		messages = view.getMessages();

		view.showModal(true);
	}

	@Override
	public void onModalReady() {
		initSlider();
		initFibreChart();
		initSelectedDevicesChart();
		initLeveeMinimap();

		clearOldSelection();

		initializeFetcher();
	}

	private void clearOldSelection() {
		for(DeviceData dd : selectedDevices.values()) {
			fibreChart.getXAxis().removePlotLine(dd.getPlotLine());
		}
		selectedDevices.clear();
		if (map != null) {
			map.reset(true);
		}


		if (fibreChart.getNativeChart() != null) {
			zoomOut(fibreChart.getNativeChart());
			zoomOut(deviceChart.getNativeChart());
		}
	}

	private native void zoomOut(JavaScriptObject nativeChart) /*-{
        nativeChart.zoomOut();
    }-*/;

	private class OverAndOutEvenHandler implements PointMouseOverEventHandler, PointMouseOutEventHandler {
		private Section selectedSection;
		private Device selectedDevice;

		@Override
		public boolean onMouseOver(PointMouseOverEvent pointMouseOverEvent) {
			selectedDevice = getDiviceForPoint(pointMouseOverEvent);
			Section oldSection = selectedSection;

			if (selectedDevice != null) {
				selectedSection = fetcher.getDeviceSection(selectedDevice);
				selectDeviceAndSection(selectedDevice, selectedSection);
			}
			unselectSection(oldSection);

			return true;
		}

		private void selectDeviceAndSection(Device device, Section section) {
			if (device != null) {
				map.addDevice(device);
				if (section != null) {
					map.highlightSection(section, true);
				} else {
					GWT.log("Device " + device.getCustomId() + " is not assigned to any section");
				}
			}
		}

		@Override
		public boolean onMouseOut(PointMouseOutEvent pointMouseOutEvent) {
			Device device = getDiviceForPoint(pointMouseOutEvent);
			if (device != null) {
				unselectDeviceAndSection(device);
			}

			return true;
		}

		private void unselectSection(Section section) {
			if (section != null && selectedSection != section) {
				map.highlightSection(section, false);
			}
		}

		private void unselectDeviceAndSection(Device device) {
			if (device != null && !selectedDevices.keySet().contains(device)) {
				map.removeDevice(device);
			}
			if(selectedDevice == device && selectedSection != null) {
				map.highlightSection(selectedSection, false);
			}
		}
	}

	private void initFibreChart() {
		if(fibreChart != null) {
			fibreChart.removeAllSeries();
			seriesCache.clear();
		} else {
			fibreChart = new Chart().
					setChartTitle(new ChartTitle().setText(messages.fibreChartTitle())).
					setWidth100();

			fibreChart.setHeight(view.getFibreDevicesHeight());

			fibreChart.getXAxis().
					setAxisTitle(new AxisTitle().setText(messages.firbreChartXAxisTitle()));

			OverAndOutEvenHandler overAndOutEvenHandler = new OverAndOutEvenHandler();

			fibreChart.setSeriesPlotOptions(new SeriesPlotOptions().
							setPointMouseOverEventHandler(overAndOutEvenHandler).
							setPointMouseOutEventHandler(overAndOutEvenHandler).
							setMarker(new Marker().
											setSelectState(new Marker().
															setFillColor(properties.selectionColor()).
															setRadius(5).
															setLineWidth(0)
											)
							).
							setAllowPointSelect(true).
							setPointSelectEventHandler(new PointSelectEventHandler() {
								@Override
								public boolean onSelect(PointSelectEvent pointSelectEvent) {
									selectDevice(getDiviceForPoint(pointSelectEvent));
									return true;
								}
							}).
							setPointUnselectEventHandler(new PointUnselectEventHandler() {
								@Override
								public boolean onUnselect(PointUnselectEvent pointUnselectEvent) {
									unselectDevice(getDiviceForPoint(pointUnselectEvent));
									return true;
								}
							})
			);

			fibreChart.setOption("/chart/zoomType", "x");

			fibreChart.setToolTip(new ToolTip()
							.setFormatter(new ToolTipFormatter() {
								private NumberFormat formatter = NumberFormat.getFormat("00.00");

								public String format(ToolTipData toolTipData) {
									String msg = "";
									for (Point point : toolTipData.getPoints()) {
										String seriesName = getSeriesName(point.getNativePoint());
										Device selectedDevice = deviceMapping.get(seriesName + "::" + toolTipData.getXAsString());
										if (selectedDevice != null) {
											msg += messages.deviceTooltip(seriesName, selectedDevice.getCableDistanceMarker() + "",
													formatter.format(point.getY()));
										}
									}
									if (msg != "") {
										return messages.devicesTooltip(toolTipData.getXAsString(), msg);
									} else {
										return null;
									}
								}

								private native String getSeriesName(JavaScriptObject point) /*-{
                                    return point.series.name;
                                }-*/;
							}).setShared(true)
			);

			view.setFibreDevices(fibreChart);
		}
	}



	private Device getDiviceForPoint(PointEvent point) {
		return deviceMapping.get(point.getSeriesName() + "::" + point.getXAsString());
	}

	private void selectDevice(final Device device) {
		selectedDevices.put(device, new DeviceData(drawDeviceLine(device)));

		selectDeviceOnMinimap(device);
		loadDeviceValues(device);
	}

	private void unselectDevice(Device device) {
		DeviceData deviceData = selectedDevices.remove(device);

		unselectDeviceOnMinimap(device);
		removePlotLine(deviceData.getPlotLine());
		removeDeviceSeries(deviceData.getSeries());
	}

	private void removeDeviceSeries(Series series) {
		if (series != null) {
			deviceChart.removeSeries(series);
		}
	}

	private void removePlotLine(PlotLine plotLine) {
		if (plotLine != null) {
			fibreChart.getXAxis().removePlotLine(plotLine);
		}
	}

	private void selectDeviceOnMinimap(Device device) {
		map.selectDevice(device, true);
	}

	private void unselectDeviceOnMinimap(Device device) {
		map.removeDevice(device);
	}

	private PlotLine drawDeviceLine(Device selectedDevice) {
		PlotLine selectedDeviceLine = fibreChart.getXAxis().createPlotLine().
				setWidth(2).setValue(selectedDevice.getLeveeDistanceMarker()).
				setColor(properties.selectionColor());

		fibreChart.getXAxis().addPlotLines(selectedDeviceLine);

		return selectedDeviceLine;
	}

	private void loadDeviceValues(final Device selectedDevice) {
		deviceChart.showLoading(messages.loadingDeviceValues(selectedDevice.getCustomId()));
		fetcher.getMeasurements(selectedDevice, slider.getStartDate(), slider.getEndDate(), new DateSeriesCallback() {
			@Override
			public void series(List<DateChartPoint> series) {
				deviceChart.hideLoading();
				DeviceData deviceData = selectedDevices.get(selectedDevice);
				if (deviceData != null) {
					Series measurements = deviceChart.createSeries().
							setName(selectedDevice.getCustomId()).
							setType(Type.SPLINE);
					for (DateChartPoint point : series) {
						measurements.addPoint(point.getX().getTime(), point.getY());
					}
					deviceChart.addSeries(measurements);
					deviceData.setSeries(measurements);
				}
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				deviceChart.showLoading(messages.errorLoadingDataFromDap());
				eventBus.showError(errorDetails);
			}
		});
	}

	private void initSelectedDevicesChart() {
		if(deviceChart != null) {
			deviceChart.removeAllSeries();
		} else {
			deviceChart = new Chart().
					setChartTitle(new ChartTitle().setText(messages.deviceChartInitTitle())).
					setWidth100();
			deviceChart.setHeight(view.getSelectedDevicesHeight());
			deviceChart.setOption("/chart/zoomType", "x");
			deviceChart.getXAxis()
					.setType(Axis.Type.DATE_TIME)
					.setDateTimeLabelFormats(new DateTimeLabelFormats()
									.setMonth("%e. %b")
									.setYear("%b")
					);

			deviceChart.setToolTip(new ToolTip().
					setPointFormat("{point.series.name}: <b>{point.y:.2f} \u00B0C<b><br/>").
					setShared(true));

			view.setSelectedDevices(deviceChart);
		}
	}

	private void initializeFetcher() {
		fibreChart.showLoading(messages.loadingFibreShare());
		fetcher.initialize(new InitializeCallback() {
			@Override
			public void ready() {
				fibreChart.getYAxis().setAxisTitle(new AxisTitle().setText(fetcher.getXAxisTitle()));
				deviceChart.getYAxis().setAxisTitle(new AxisTitle().setText(fetcher.getXAxisTitle()));
				customizeYAxis(fibreChart.getNativeChart(), deviceChart.getNativeChart());
				fibreChart.removeAllSeries();
				fibreChart.hideLoading();
				loadData(slider.getSelectedDate());
				showSections(fetcher.getSections());
				showDeviceAggregations();
			}

			private void showSections(Collection<Section> sections) {
				for (Section section : sections) {
					map.addSection(section);
				}
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
				fibreChart.showLoading(messages.errorLoadingDataFromDap());
			}
		});
	}

	private void showDeviceAggregations() {
		for(DeviceAggregate da : fetcher.getDeviceAggregations()) {
			map.addDeviceAggregate(da);
		}
	}

	private void initSlider() {
		if (slider == null) {
			slider = eventBus.addHandler(SliderPresenter.class);
			slider.setEventsListener(new SliderPresenter.Events() {
				@Override
				public void onDateChanged(Date selectedDate) {
					onSliderChanged(selectedDate);
				}

				@Override
				public void onStartDateChanged(Date startDate) {
					updateSelectedDevicesSeries();
				}

				@Override
				public void onEndDateChanged(Date endDate) {
					updateSelectedDevicesSeries();
				}
			});
			view.setSlider(slider.getView());
		}
	}

	private void updateSelectedDevicesSeries() {
		Collection<Device> selected = selectedDevices.keySet();
		if (selected.size() > 0) {
			updateDevicesSeries(selected);
		}
	}

	private void updateDevicesSeries(Collection<Device> devices) {
		deviceChart.showLoading(messages.loadingDevicesValues());
		fetcher.getMeasurements(devices, slider.getStartDate(), slider.getEndDate(), new DevicesDateSeriesCallback() {
			@Override
			public void series(Map<Device, List<DateChartPoint>> series) {
				deviceChart.hideLoading();
				deviceChart.removeAllSeries();

				for (Map.Entry<Device, List<DateChartPoint>> s : series.entrySet()) {
					DeviceData deviceData = selectedDevices.get(s.getKey());
					if (deviceData != null) {
						Series measurements = deviceChart.createSeries().
								setName(s.getKey().getCustomId()).
								setType(Type.SPLINE);
						for (DateChartPoint point : s.getValue()) {
							measurements.addPoint(point.getX().getTime(), point.getY());
						}
						deviceChart.addSeries(measurements);
						deviceData.setSeries(measurements);
					}
				}
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
		});
	}

	private void initLeveeMinimap() {
		if (map == null) {
			map = eventBus.addHandler(MapPresenter.class);
			view.setMap(map.getView());
		}
	}

	@Override
	public void onSliderChanged(final Date selectedDate) {
		loadData(selectedDate);
	}

	private void loadData(Date selectedDate) {
		fibreChart.showLoading(messages.loadingData());
		fetcher.getSeries(selectedDate, new SeriesCallback() {
			@Override
			public void series(Map<DeviceAggregate, List<IDataFetcher.ChartPoint>> series) {
				deviceMapping.clear();
				Map<String, Series> newSeriesCache = new HashMap<>();
				for (Map.Entry<DeviceAggregate, List<IDataFetcher.ChartPoint>> points : series.entrySet()) {
					DeviceAggregate aggregation = points.getKey();
					Series s = getSeries(aggregation);
					newSeriesCache.put(aggregation.getId(), s);
					upateSeries(s, points);
				}
				for (Series s : seriesCache.values()) {
					fibreChart.removeSeries(s, false);
				}
				seriesCache = newSeriesCache;
				fibreChart.hideLoading();
				fibreChart.redraw();
			}

			@Override
			public void noData() {
				for (Series s : seriesCache.values()) {
					fibreChart.removeSeries(s, false);
				}
				seriesCache = new HashMap<String, Series>();
				fibreChart.showLoading(messages.fibreNoData());
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
				fibreChart.showLoading(messages.errorLoadingDataFromDap());
			}
		});
	}

	private Series getSeries(DeviceAggregate aggregation) {
		Series s = seriesCache.remove(aggregation.getId());
		if (s == null) {
			s = fibreChart.createSeries().
					setName(aggregation.getCustomId()).
					setType(Type.SPLINE);
		}

		return s;
	}

	private void upateSeries(Series s, Map.Entry<DeviceAggregate, List<IDataFetcher.ChartPoint>> points) {
		DeviceAggregate aggregation = points.getKey();
		List<IDataFetcher.ChartPoint> newPoints = points.getValue();

		if (theSameX(s.getPoints(), newPoints)) {
			Point[] seriesPoints = s.getPoints();
			for (int i = 0; i < newPoints.size(); i++) {
				Point seriesPoint = seriesPoints[i];
				ChartPoint newPoint = newPoints.get(i);
				seriesPoint.update(newPoint.getX(), newPoint.getY(), false);
				deviceMapping.put(aggregation.getCustomId() + "::" + newPoint.getX(), newPoint.getDevice());
			}
		} else {
			s.remove();
			for (ChartPoint point : newPoints) {
				s.addPoint(point.getX(), point.getY());
				deviceMapping.put(aggregation.getCustomId() + "::" + point.getX(), point.getDevice());
			}
			fibreChart.addSeries(s, false, true);
		}
	}

	private boolean theSameX(Point[] points, List<IDataFetcher.ChartPoint> newPoints) {
		return points !=null && newPoints != null &&
				points.length == newPoints.size();
	}

	private native void customizeYAxis(JavaScriptObject fiberChart, JavaScriptObject deviceChart) /*-{
		fiberChart.yAxis[0].update({
			showEmpty: false
		});
		deviceChart.yAxis[0].update({
			showEmpty: false
		});
	}-*/;
}