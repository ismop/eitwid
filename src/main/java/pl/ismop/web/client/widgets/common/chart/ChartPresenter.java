package pl.ismop.web.client.widgets.common.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Axis.Type;
import org.moxieapps.gwt.highcharts.client.BaseChart.ZoomType;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Extremes;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOutEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOutEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOverEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.IsmopWebEntryPoint;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.widgets.common.chart.IChartView.IChartPresenter;
import pl.ismop.web.client.widgets.common.timeinterval.TimeIntervalPresenter;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus>
		implements IChartPresenter, ChartSelectionEventHandler {

	private final IsmopConverter converter;

	private Chart chart;
	
	private int height;
	
	private BiMap<String, ChartSeries> dataSeriesMap;
	
	private Map<String, Series> chartSeriesMap;
	
	private Map<String, Integer> yAxisMap;
	
	private ZoomDataCallback zoomDataCallback;

	private PlotLine currentTimePlotLine;

	private DeviceSelectHandler deviceSelectHandler;

	public interface ZoomDataCallback {
		void onZoom(Date startDate, Date endDate, List<String> timelineIds, DataCallback callback);
	}

	public interface DeviceSelectHandler {
		void select(ChartSeries series);
		void unselect(ChartSeries series);
	}

	public interface DataCallback {
		void updateData(Map<String, Number[][]> data);
	}

	@Inject
	public ChartPresenter(IsmopConverter converter) {
		this.converter = converter;

		dataSeriesMap = HashBiMap.create();
		chartSeriesMap = new HashMap<>();
		yAxisMap = new HashMap<>();
	}

	public void addChartSeries(ChartSeries series) {
		addChartSeries(series, true);
	}
	
	public void addChartSeriesList(List<ChartSeries> series) {
		for (ChartSeries chartSeries : series) {
			addChartSeries(chartSeries, false);
		}
		
		chart.redraw();
	}

	public void initChart() {
		if (chart == null) {
			chart = new Chart()
					.setType(Series.Type.SPLINE)
					.setTitle(
							new ChartTitle().setText(view.getChartTitle()),
							new ChartSubtitle())
					.setZoomType(ZoomType.X)
					.setLegend(new Legend()
							.setMaxHeight(40))
					.setSelectionEventHandler(this);

			chart.getXAxis().setType(Type.DATE_TIME);

			if(height > 0) {
				chart.setHeight(height);
			}

			chart.setSeriesPlotOptions(new SeriesPlotOptions()
					.setSeriesMouseOverEventHandler(new SeriesMouseOverEventHandler() {
						@Override
						public boolean onMouseOver(SeriesMouseOverEvent event) {
							getDeviceSelectHandler().select(dataSeriesMap.get(event.getSeriesId()));
							return true;
						}
					})
					.setSeriesMouseOutEventHandler(new SeriesMouseOutEventHandler() {
						@Override
						public boolean onMouseOut(SeriesMouseOutEvent event) {
							getDeviceSelectHandler().unselect(dataSeriesMap.get(event.getSeriesId()));
							return true;
						}
					})
			);
			chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
				@Override
				public String format(ToolTipData toolTipData) {
					String msg = converter.formatForDisplay(new Date(toolTipData.getXAsLong())) + "<br/>";
					for (Point point : toolTipData.getPoints()) {
						JavaScriptObject nativePoint = point.getNativePoint();
						msg += "<br/><span style=\"color:" + getPointColor(nativePoint) +
								"\">\u25CF</span> " + getSeriesName(nativePoint) + ": <b>" +
								NumberFormat.getFormat("0.00").format(point.getY()) + " " +
								dataSeriesMap.get(getSeriesId(nativePoint)).getUnit() + "</b><br/>";
					}
					return msg;
				}

				private native String getSeriesId(JavaScriptObject point) /*-{
                    return point.series.options.id;
                }-*/;

				private native String getSeriesName(JavaScriptObject point) /*-{
                    return point.series.name;
                }-*/;

				private native String getPointColor(JavaScriptObject nativePoint) /*-{
                    return nativePoint.color;
                }-*/;
			}).setShared(true));
			chart.setOption("exporting/buttons/contextButton/menuItems", getExportCSVChartBtn());
			view.addChart(chart);
		}
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void removeChartSeriesForDevice(Device device) {
		for (String chartSeriesKey : new ArrayList<>(dataSeriesMap.keySet())) {
			if (dataSeriesMap.get(chartSeriesKey).getDeviceId().equals(device.getId())) {
				dataSeriesMap.remove(chartSeriesKey);
				chart.removeSeries(chartSeriesMap.remove(chartSeriesKey));
			}
		}

		clearDataSeries();
	}

	public void removeChartSeriesForParameter(Parameter parameter) {
		for (String chartSeriesKey : new ArrayList<>(dataSeriesMap.keySet())) {
			if (dataSeriesMap.get(chartSeriesKey).getParameterId().equals(parameter.getId())) {
				dataSeriesMap.remove(chartSeriesKey);
				chart.removeSeries(chartSeriesMap.remove(chartSeriesKey));
			}
		}

		clearDataSeries();
	}

	public void showLoading(String msg) {
		chart.showLoading(msg);
	}

	public void hideLoading() {
		chart.hideLoading();
	}

	public void zoomOut() {
		zoomOut(chart.getNativeChart());
	}

	public int getSeriesCount() {
		return dataSeriesMap.size();
	}

	public List<ChartSeries> getSeries() {
		return new ArrayList<>(dataSeriesMap.values());
	}

	public void reset() {
		for (String chartSeriesKey : new ArrayList<>(dataSeriesMap.keySet())) {
			dataSeriesMap.remove(chartSeriesKey);
			chart.removeSeries(chartSeriesMap.remove(chartSeriesKey));
		}
		
		yAxisMap.clear();
		
		if (chart != null) {
			chart.removeAllSeries();
		}
	}
	
	public void setLoadingState(boolean loading) {
		if (chart != null) {
			if (loading) {
				chart.showLoading(view.getLoadingMessage());
			} else {
				chart.hideLoading();
			}
		} else {
			view.showLoadingMessage(loading);
		}
	}
	
	public void setZoomDataCallback(ZoomDataCallback zoomDataCallback) {
		this.zoomDataCallback = zoomDataCallback;
	}

	@Override
	public boolean onSelection(ChartSelectionEvent chartSelectionEvent) {
		if (zoomDataCallback != null) {
			if (hasXAxis(chartSelectionEvent.getNativeEvent())) {
				long startMillis = chartSelectionEvent.getXAxisMinAsLong();
				long endMillis = chartSelectionEvent.getXAxisMaxAsLong();
				setLoadingState(true);
				zoomDataCallback.onZoom(new Date(startMillis), new Date(endMillis),
						collectTimelineIds(), new DataCallback() {
					@Override
					public void updateData(Map<String, Number[][]> data) {
						setLoadingState(false);
						
						for (String timelineId : data.keySet()) {
							for (String chartSeriesId : dataSeriesMap.keySet()) {
								if (dataSeriesMap.get(chartSeriesId).getTimelineId().equals(timelineId)) {
									chartSeriesMap.get(chartSeriesId).setPoints(data.get(timelineId));
									
									break;
								}
							}
						}
					}
				});
			} else {
				//zoom was just reset
				for (String chartSeriesId : dataSeriesMap.keySet()) {
					chartSeriesMap.get(chartSeriesId).setPoints(
							dataSeriesMap.get(chartSeriesId).getValues());
				}
			}
		}
		
		return true;
	}

	public void setInterval(Date startDate, Date endDate) {
		initChart();
		chart.getXAxis().setMin(startDate.getTime());
        chart.getXAxis().setMax(endDate.getTime());
	}

	public void setDeviceSelectHandler(DeviceSelectHandler deviceSelectHandler) {
		this.deviceSelectHandler = deviceSelectHandler;
	}

	public DeviceSelectHandler getDeviceSelectHandler() {
		if (deviceSelectHandler == null) {
			return new DeviceSelectHandler() {
				@Override
				public void select(ChartSeries series) {}
				@Override
				public void unselect(ChartSeries series) {}
			};
		}

		return deviceSelectHandler;
	}

	public void selectDate(Date selectedDate, String color) {
		initChart();
		
		if (currentTimePlotLine != null) {
			chart.getXAxis().removePlotLine(currentTimePlotLine);
		}
		
		currentTimePlotLine = chart.getXAxis().createPlotLine().
				setWidth(2).setColor(color).setValue(selectedDate.getTime());
		chart.getXAxis().addPlotLines(currentTimePlotLine);
	}

	private void exportCSV() {
		TimeIntervalPresenter timeInterval = eventBus.addHandler(TimeIntervalPresenter.class);
		timeInterval.show(getChartFrom(), getChartTo(),
				(Date from, Date to) -> exportCSV(from, to));
	}

	private Date getChartFrom() {
		Extremes xExtremes = chart.getXAxis().getExtremes();
		Date from = new Date();
		if(xExtremes.getDataMin() != null) {
			from = new Date(xExtremes.getDataMin().longValue());
		}

		return from;
	}

	private Date getChartTo() {
		Extremes xExtremes = chart.getXAxis().getExtremes();
		Date to = new Date();
		if(xExtremes.getDataMax() != null) {
			to = new Date(xExtremes.getDataMax().longValue());
		}

		return to;
	}

	private void exportCSV(Date from, Date to) {
		List<String> parameterIds = new ArrayList<>();

		for (ChartSeries chartSeries : getSeries()) {
			parameterIds.add(chartSeries.getParameterId());
		}

		Window.open(IsmopWebEntryPoint.properties.get("dapEndpoint") +
				"/chart_exporter?time_from=" + converter.formatForDto(from) +
				"&time_to=" + converter.formatForDto(to) +
				"&parameters=" + converter.merge(parameterIds) +
				"&private_token=" + IsmopWebEntryPoint.properties.get("dapToken"), "_self", null);

	}

	private String getDownloadCSVMessage() {
		return getView().getDownloadCSVMessage();
	}

	private Number getYAxisIndex(ChartSeries series) {
		String yAxisLabel = series.getLabel() + ", [" + series.getUnit() + "]";
		Number result;
		
		if(yAxisMap.containsKey(yAxisLabel)) {
			result = yAxisMap.get(yAxisLabel);
		} else {
			int index = yAxisMap.size();
			
			if(index == 0) {
				updateFirstYAxis(chart.getNativeChart(), yAxisLabel);
			} else {
				addAxis(chart.getNativeChart(), index, yAxisLabel);
			}
			
			yAxisMap.put(yAxisLabel, index);
			
			result = index;
		}
		
		return result;
	}

	private void clearDataSeries() {
		if (dataSeriesMap.size() == 0) {
			yAxisMap.clear();
			chart.removeAllSeries();
		}
	}
	
	private List<String> collectTimelineIds() {
		List<String> result = new ArrayList<>();
		
		for (ChartSeries chartSeries : dataSeriesMap.values()) {
			result.add(chartSeries.getTimelineId());
		}
		
		return result;
	}

	private void addChartSeries(ChartSeries series, boolean redraw) {
		initChart();
		
		Series chartSeries = null;
		Optional<ChartSeries> foundChartSeries = Iterables.tryFind(dataSeriesMap.values(),
				s -> s.getParameterId().equals(series.getParameterId()));
	
		if (foundChartSeries.isPresent()) {
			chartSeries = chartSeriesMap.get(dataSeriesMap.inverse().get(foundChartSeries.get()));
		} else {
			chartSeries = chart.createSeries();
			chartSeriesMap.put(chartSeries.getId(), chartSeries);
			dataSeriesMap.put(chartSeries.getId(), series);
		}
		
		chartSeries
			.setName(series.getName())
			.setPoints(series.getValues())
			.setYAxis(getYAxisIndex(series));
		
		if (!foundChartSeries.isPresent()) {
			chart.addSeries(chartSeries, redraw, true);
		}
	}
	
	private void yAxisClicked(String axisLabel) {
		Integer axisIndex = yAxisMap.get(axisLabel);
		
		for (Series series : chart.getSeries()) {
			if (String.valueOf(axisIndex).equals(series.getOptions().get("yAxis").toString())) {
				//going only through series attached to the given y axis
				if (series.isVisible()) {
					series.hide();
				} else {
					series.show();
				}
			}
		}
	}

	private native JavaScriptObject getExportCSVChartBtn() /*-{
		var thisObject = this
		var exports = $wnd.Highcharts.getOptions().exporting.buttons.contextButton.menuItems.slice(0)
	    exports.push({
	        text: thisObject.@pl.ismop.web.client.widgets.common.chart.ChartPresenter::getDownloadCSVMessage()(),
	        onclick: function () {
	            thisObject.@pl.ismop.web.client.widgets.common.chart.ChartPresenter::exportCSV()()
	        }
	    })
		return exports
	}-*/;

	private native void updateFirstYAxis(JavaScriptObject nativeChart, String yAxisLabel) /*-{
		var thisObject = this;
		nativeChart.yAxis[0].update({
			title: {
				text: yAxisLabel,
				style: {
					cursor: "pointer"
				},
				events: {
					click: function() {
						thisObject.@pl.ismop.web.client.widgets.common.chart.ChartPresenter::yAxisClicked(Ljava/lang/String;)(yAxisLabel);
					}
				}
			},
			labels: {
				format: "{value:.2f}"
			}
		});
	}-*/;

	private native void addAxis(JavaScriptObject nativeChart, int index, String yAxisLabel) /*-{
		var thisObject = this;
		nativeChart.addAxis({
			title: {
				text: yAxisLabel,
				style: {
					cursor: "pointer"
				},
				events: {
					click: function() {
						thisObject.@pl.ismop.web.client.widgets.common.chart.ChartPresenter::yAxisClicked(Ljava/lang/String;)(yAxisLabel);
					}
				}
			},
			labels: {
				format: "{value:.2f}"
			}
		});
	}-*/;

	private native void zoomOut(JavaScriptObject nativeChart) /*-{
        nativeChart.zoomOut();
    }-*/;

	private native boolean hasXAxis(JavaScriptObject selectionEvent) /*-{
		return selectionEvent.xAxis != null;
	}-*/;
}