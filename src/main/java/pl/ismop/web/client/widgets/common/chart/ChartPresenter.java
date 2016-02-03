package pl.ismop.web.client.widgets.common.chart;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import org.moxieapps.gwt.highcharts.client.Axis.Type;
import org.moxieapps.gwt.highcharts.client.BaseChart.ZoomType;
import org.moxieapps.gwt.highcharts.client.*;
import org.moxieapps.gwt.highcharts.client.events.*;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.widgets.common.chart.IChartView.IChartPresenter;

import java.util.*;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus>
		implements IChartPresenter, ChartSelectionEventHandler {

	private Chart chart;
	
	private int height;
	
	private Map<String, ChartSeries> dataSeriesMap;
	
	private Map<String, Series> chartSeriesMap;
	
	private Map<String, Integer> yAxisMap;
	
	private boolean seriesHoverListener;
	
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

	public ChartPresenter() {
		dataSeriesMap = new HashMap<>();
		chartSeriesMap = new HashMap<>();
		yAxisMap = new HashMap<>();

		deviceSelectHandler = new DeviceSelectHandler() {

			@Override
			public void select(ChartSeries series) {
				eventBus.deviceSeriesHover(series.getDeviceId(), true);
			}

			@Override
			public void unselect(ChartSeries series) {
				eventBus.deviceSeriesHover(series.getDeviceId(), false);
			}
		};
	}

	public void addChartSeries(ChartSeries series) {
		initChart();

		Series chartSeries = chart.createSeries();
		chartSeriesMap.put(chartSeries.getId(), chartSeries);
		dataSeriesMap.put(chartSeries.getId(), series);
		
		chartSeries
			.setName(series.getName())
			.setPoints(series.getValues())
			.setYAxis(getYAxisIndex(series));
		chart.addSeries(chartSeries);
	}

	public void initChart() {
		if(chart == null) {
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
							if(seriesHoverListener) {
								deviceSelectHandler.select(dataSeriesMap.get(event.getSeriesId()));
							}

							return true;
						}
					})
					.setSeriesMouseOutEventHandler(new SeriesMouseOutEventHandler() {
						@Override
						public boolean onMouseOut(SeriesMouseOutEvent event) {
							if(seriesHoverListener) {
								deviceSelectHandler.unselect(dataSeriesMap.get(event.getSeriesId()));
							}

							return true;
						}
					})
			);
			chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
				@Override
				public String format(ToolTipData toolTipData) {
					String msg = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(new Date(toolTipData.getXAsLong())) + "<br/>";
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
			view.addChart(chart);
		}
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void removeChartSeriesForDevice(Device device) {
		for(String chartSeriesKey : new ArrayList<>(dataSeriesMap.keySet())) {
			if(dataSeriesMap.get(chartSeriesKey).getDeviceId().equals(device.getId())) {
				dataSeriesMap.remove(chartSeriesKey);
				chart.removeSeries(chartSeriesMap.remove(chartSeriesKey));
			}
		}

		clearDataSeries();
	}

	public void removeChartSeriesForParameter(Parameter parameter) {
		for(String chartSeriesKey : new ArrayList<>(dataSeriesMap.keySet())) {
			if(dataSeriesMap.get(chartSeriesKey).getParameterId().equals(parameter.getId())) {
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
		for(String chartSeriesKey : new ArrayList<>(dataSeriesMap.keySet())) {
			dataSeriesMap.remove(chartSeriesKey);
			chart.removeSeries(chartSeriesMap.remove(chartSeriesKey));
		}
		
		yAxisMap.clear();
		
		if(chart != null) {
			chart.removeAllSeries();
		}
	}
	
	public void addSeriesHoverListener() {
		seriesHoverListener = true;
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

	private Number getYAxisIndex(ChartSeries series) {
		String yAxisLabel = series.getLabel() + ", [" + series.getUnit() + "]";
		
		if(yAxisMap.containsKey(yAxisLabel)) {
			return yAxisMap.get(yAxisLabel);
		} else {
			int index = yAxisMap.size();
			
			if(index == 0) {
				updateFirstYAxis(chart.getNativeChart(), yAxisLabel);
			} else {
				addAxis(chart.getNativeChart(), index, yAxisLabel);
			}
			
			yAxisMap.put(yAxisLabel, index);
			
			return index;
		}
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

	private native void updateFirstYAxis(JavaScriptObject nativeChart, String yAxisLabel) /*-{
		nativeChart.yAxis[0].update({
			title: {
				text: yAxisLabel
			},
			labels: {
				format: "{value:.2f}"
			},
			showEmpty: false
		});
	}-*/;

	private native void addAxis(JavaScriptObject nativeChart, int index, String yAxisLabel) /*-{
		nativeChart.addAxis({
			showEmpty: false,
			title: {
				text: yAxisLabel
			},
			labels: {
				format: "{value:.2f}"
			},
			showEmpty: false
		});
	}-*/;

	private native void zoomOut(JavaScriptObject nativeChart) /*-{
        nativeChart.zoomOut();
    }-*/;

	private native boolean hasXAxis(JavaScriptObject selectionEvent) /*-{
		return selectionEvent.xAxis != null;
	}-*/;
}