package pl.ismop.web.client.widgets.common.chart;

import java.util.HashMap;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Axis.Type;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Series;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.widgets.common.chart.IChartView.IChartPresenter;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus> implements IChartPresenter {
	private Chart chart;
	private int height;
	private Map<String, ChartSeries> dataSeriesMap;
	private Map<String, Series> chartSeriesMap;
	private Map<String, Integer> yAxisMap;
	
	public ChartPresenter() {
		dataSeriesMap = new HashMap<>();
		chartSeriesMap = new HashMap<>();
		yAxisMap = new HashMap<>();
	}

	public void addChartSeries(ChartSeries series) {
		if(chart == null) {
			chart = new Chart()
					.setType(Series.Type.LINE)
					.setTitle(
							new ChartTitle().setText(view.getChartTitle()),
							new ChartSubtitle());
			chart.getXAxis().setType(Type.DATE_TIME);
			
			if(height > 0) {
				chart.setHeight(height);
			}
			
			view.addChart(chart);
		}
		
		String key = createKey(series);
		Series chartSeries = chart.createSeries();
		chartSeriesMap.put(key, chartSeries);
		dataSeriesMap.put(key, series);
		
		chart.addSeries(
				chartSeries
					.setName(series.getDeviceId())
					.setPoints(series.getValues())
					.setYAxis(getYAxisIndex(series))
		);
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void removeChartSeriesForDevice(Device device) {
		for(String chartSeriesKey : dataSeriesMap.keySet()) {
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

	public int getSeriesCount() {
		return dataSeriesMap.size();
	}

	private Number getYAxisIndex(ChartSeries series) {
		String yAxisLabel = series.getLabel() + ", [" + series.getUnit() + "]";
		
		if(yAxisMap.containsKey(yAxisLabel)) {
			return yAxisMap.get(yAxisLabel);
		} else {
			int index = yAxisMap.size();
			yAxisMap.put(yAxisLabel, index);
			chart.getYAxis(index).setAxisTitle(new AxisTitle().setText(yAxisLabel));
			
			return index;
		}
	}

	private String createKey(ChartSeries series) {
		return series.getDeviceId() + series.getParameterId();
	}
}