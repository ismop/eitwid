package pl.ismop.web.client.widgets.common.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Axis.Type;
import org.moxieapps.gwt.highcharts.client.BaseChart.ZoomType;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOutEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOutEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesMouseOverEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.core.client.JavaScriptObject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.widgets.common.chart.IChartView.IChartPresenter;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus> implements IChartPresenter {
	private Chart chart;
	private int height;
	private Map<String, ChartSeries> dataSeriesMap;
	private Map<String, Series> chartSeriesMap;
	private Map<String, Integer> yAxisMap;
	private boolean seriesHoverListener;
	
	public ChartPresenter() {
		dataSeriesMap = new HashMap<>();
		chartSeriesMap = new HashMap<>();
		yAxisMap = new HashMap<>();
	}

	public void addChartSeries(ChartSeries series) {
		if(chart == null) {
			chart = new Chart()
					.setType(Series.Type.SPLINE)
					.setTitle(
							new ChartTitle().setText(view.getChartTitle()),
							new ChartSubtitle())
					.setZoomType(ZoomType.X);
			chart.getXAxis().setType(Type.DATE_TIME);
			
			if(height > 0) {
				chart.setHeight(height);
			}
			
			chart.setSeriesPlotOptions(new SeriesPlotOptions()
				.setSeriesMouseOverEventHandler(new SeriesMouseOverEventHandler() {
					@Override
					public boolean onMouseOver(SeriesMouseOverEvent event) {
						if(seriesHoverListener) {
							eventBus.deviceSeriesHover(dataSeriesMap.get(event.getSeriesId()).getDeviceId(), true);
						}
						
						return true;
					}
				})
				.setSeriesMouseOutEventHandler(new SeriesMouseOutEventHandler() {
					@Override
					public boolean onMouseOut(SeriesMouseOutEvent event) {
						if(seriesHoverListener) {
							eventBus.deviceSeriesHover(dataSeriesMap.get(event.getSeriesId()).getDeviceId(), false);
						}
						
						return true;
					}
				})
			);
			view.addChart(chart);
		}
		
		Series chartSeries = chart.createSeries();
		chartSeriesMap.put(chartSeries.getId(), chartSeries);
		dataSeriesMap.put(chartSeries.getId(), series);
		
		chartSeries
			.setName(series.getName())
			.setPoints(series.getValues())
			.setYAxis(getYAxisIndex(series));
		chart.addSeries(chartSeries);
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
		
		if(dataSeriesMap.size() == 0) {
			yAxisMap.clear();
			chart.removeAllSeries();
			chart.removeFromParent();
			chart = null;
		}
	}

	public void removeChartSeriesForParameter(Parameter parameter) {
		for(String chartSeriesKey : new ArrayList<>(dataSeriesMap.keySet())) {
			if(dataSeriesMap.get(chartSeriesKey).getParameterId().equals(parameter.getId())) {
				dataSeriesMap.remove(chartSeriesKey);
				chart.removeSeries(chartSeriesMap.remove(chartSeriesKey));
			}
		}
		
		if(dataSeriesMap.size() == 0) {
			yAxisMap.clear();
			chart.removeAllSeries();
			chart.removeFromParent();
			chart = null;
		}
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
			chart.removeFromParent();
			chart = null;
		}
	}
	
	public void addSeriesHoverListener() {
		seriesHoverListener = true;
	}

	public void setLoadingState(boolean loading) {
		if(chart != null) {
			if(loading) {
				chart.showLoading(view.getLoadingMessage());
			} else {
				chart.hideLoading();
			}
		} else {
			view.showLoadingMessage(loading);
		}
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

	private native void updateFirstYAxis(JavaScriptObject nativeChart, String yAxisLabel) /*-{
		nativeChart.yAxis[0].update({
			title: {
				text: yAxisLabel
			},
			labels: {
				format: "{value:.2f}"
			}
		});
	}-*/;

	private native void addAxis(JavaScriptObject nativeChart, int index, String yAxisLabel) /*-{
		nativeChart.addAxis({
			title: {
				text: yAxisLabel
			},
			labels: {
				format: "{value:.2f}"
			}
		});
	}-*/;
}