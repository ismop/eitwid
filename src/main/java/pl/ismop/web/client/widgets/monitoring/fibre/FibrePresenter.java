package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import org.moxieapps.gwt.highcharts.client.*;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.events.*;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;
import pl.ismop.web.client.IsmopProperties;
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

import java.util.*;

import static pl.ismop.web.client.widgets.monitoring.fibre.IDataFetcher.*;

@Presenter(view = FibreView.class)
public class FibrePresenter extends BasePresenter<IFibreView, MainEventBus> implements IFibrePresenter {
	private class DeviceData {
		private PlotBand plotBand;
		private Series series;

		public DeviceData(PlotBand plotBand) {
			this.plotBand = plotBand;
		}

		public PlotBand getPlotBand() {
			return plotBand;
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

		initSlider();
		initFibreChart();
		initDeviceChart();


		view.showModal(true);
	}

	@Override
	public void onModalReady() {
		initLeveeMinimap();
		fibreChart.reflow();
		deviceChart.reflow();

		initializeFetcher();
	}

	private void initFibreChart() {
		if(fibreChart != null) {
			fibreChart.removeAllSeries();
			seriesCache.clear();
		} else {
			fibreChart = new Chart().
					setChartTitle(new ChartTitle().setText(messages.fibreChartTitle())).
					setWidth100();

			fibreChart.getXAxis().
					setAxisTitle(new AxisTitle().setText(messages.firbreChartXAxisTitle()));

			fibreChart.setSeriesPlotOptions(new SeriesPlotOptions().
							setPointMouseOverEventHandler(new PointMouseOverEventHandler() {
								private Section selectedSection;
								private Device selectedDevice;

								@Override
								public boolean onMouseOver(PointMouseOverEvent pointMouseOverEvent) {
									Device selectedDevice = getDiviceForPoint(pointMouseOverEvent);
									Section selectedSection = null;

									if (selectedDevice != null) {
										selectedSection = fetcher.getDeviceSection(selectedDevice);
										selectDeviceAndSection(selectedDevice, selectedSection);
									}
									unselectOldSection(selectedSection);
									unselectOldDevice(selectedDevice);

									this.selectedDevice = selectedDevice;
									this.selectedSection = selectedSection;

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

								private void unselectOldSection(Section newSelectedSection) {
									if (this.selectedSection != null && this.selectedSection != newSelectedSection) {
										map.highlightSection(this.selectedSection, false);
									}
								}

								private void unselectOldDevice(Device newSelectedDevice) {
									if (this.selectedDevice != null && this.selectedDevice != newSelectedDevice &&
											!selectedDevices.keySet().contains(this.selectedDevice)) {
										map.removeDevice(this.selectedDevice);
									}
								}
							}).
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
								public String format(ToolTipData toolTipData) {
									Device selectedDevice = deviceMapping.get(toolTipData.getSeriesName() + "::" + toolTipData.getXAsString());
									if (selectedDevice != null) {
										return "<b>" + toolTipData.getYAsString() + "\u00B0C</b><br/>" +
												selectedDevice.getLeveeDistanceMarker() + " metr wału<br/>" +
												selectedDevice.getCableDistanceMarker() + " metr światłowodu<br/>" +
												"Sensor: " + selectedDevice.getCustomId();
									} else {
										return null;
									}
								}
							})
			);

			view.addElementToLeftPanel(fibreChart);
		}
	}



	private Device getDiviceForPoint(PointEvent point) {
		return deviceMapping.get(point.getSeriesName() + "::" + point.getXAsString());
	}

	private void selectDevice(final Device device) {
		selectedDevices.put(device, new DeviceData(drawDeviceBand(device)));

		selectDeviceOnMinimap(device);
		loadDeviceValues(device);
	}

	private void unselectDevice(Device device) {
		DeviceData deviceData = selectedDevices.remove(device);

		unselectDeviceOnMinimap(device);
		removePlotBand(deviceData.getPlotBand());
		removeDeviceSeries(deviceData.getSeries());
	}

	private void removeDeviceSeries(Series series) {
		if (series != null) {
			deviceChart.removeSeries(series);
		}
	}

	private void removePlotBand(PlotBand plotBand) {
		if (plotBand != null) {
			fibreChart.getXAxis().removePlotBand(plotBand);
		}
	}

	private void selectDeviceOnMinimap(Device device) {
		map.selectDevice(device, true);
	}

	private void unselectDeviceOnMinimap(Device device) {
		map.removeDevice(device);
	}

	private PlotBand drawDeviceBand(Device selectedDevice) {
		PlotBand selectedDeviceBand = fibreChart.getXAxis().createPlotBand().
				setFrom(selectedDevice.getLeveeDistanceMarker() - 0.1).
				setTo(selectedDevice.getLeveeDistanceMarker() + 0.1).
				setColor(properties.selectionColor());

		fibreChart.getXAxis().addPlotBands(selectedDeviceBand);

		return selectedDeviceBand;
	}

	private void loadDeviceValues(final Device selectedDevice) {
		deviceChart.showLoading(messages.loadingDeviceValues(selectedDevice.getCustomId()));
		fetcher.getMeasurements(selectedDevice, slider.getStartDate(), slider.getEndDate(), new DateSeriesCallback() {
			@Override
			public void series(List<IDataFetcher.DateChartPoint> series) {
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

	private void initDeviceChart() {
		if(deviceChart != null) {
			deviceChart.removeAllSeries();
		} else {
			deviceChart = new Chart().
					setChartTitle(new ChartTitle().setText(messages.deviceChartInitTitle())).
					setWidth100();
			deviceChart.setOption("/chart/zoomType", "x");
			deviceChart.getXAxis()
					.setType(Axis.Type.DATE_TIME)
					.setDateTimeLabelFormats(new DateTimeLabelFormats()
									.setMonth("%e. %b")
									.setYear("%b")
					);

			view.addElementToLeftPanel(deviceChart);
		}
	}

	private void initializeFetcher() {
		fibreChart.showLoading(messages.loadingFibreShare());
		fetcher.initialize(new InitializeCallback() {
			@Override
			public void ready() {
				fibreChart.getYAxis().setAxisTitle(new AxisTitle().setText(fetcher.getXAxisTitle()));
				deviceChart.getYAxis().setAxisTitle(new AxisTitle().setText(fetcher.getXAxisTitle()));
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
		for(DeviceAggregation da : fetcher.getDeviceAggregations()) {
			map.addDeviceAggregation(da);
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
			view.addElementToLeftPanel(slider.getView());
		}
	}

	private void updateSelectedDevicesSeries() {
		deviceChart.showLoading(messages.loadingDevicesValues());
		Collection<Device> selected = selectedDevices.keySet();
		fetcher.getMeasurements(selected, slider.getStartDate(), slider.getEndDate(), new DevicesDateSeriesCallback() {
			@Override
			public void series(Map<Device, List<IDataFetcher.DateChartPoint>> series) {
				deviceChart.hideLoading();
				deviceChart.removeAllSeries();

				for(Map.Entry<Device, List<IDataFetcher.DateChartPoint>> s : series.entrySet()) {
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
			view.addElementToRightPanel(map.getView());
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
					fibreChart.removeSeries(s, false);
				}
				seriesCache = newSeriesCache;
				fibreChart.hideLoading();
				fibreChart.redraw();
			}

			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
				fibreChart.showLoading(messages.errorLoadingDataFromDap());
			}
		});
	}

	private Series getSeries(DeviceAggregation aggregation) {
		Series s = seriesCache.remove(aggregation.getId());
		if (s == null) {
			s = fibreChart.createSeries().
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
				ChartPoint newPoint = newPoints.get(i);
				seriesPoint.update(newPoint.getX(), newPoint.getY(), false);
				deviceMapping.put(aggregation.getId() + "::" + newPoint.getX(), newPoint.getDevice());
			}
		} else {
			s.remove();
			for (ChartPoint point : newPoints) {
				s.addPoint(point.getX(), point.getY());
				deviceMapping.put(aggregation.getId() + "::" + point.getX(), point.getDevice());
			}
			fibreChart.addSeries(s, false, true);
		}
	}

	private boolean theSameX(Point[] points, List<IDataFetcher.ChartPoint> newPoints) {
		return points !=null && newPoints != null &&
				points.length == newPoints.size();
	}
}