package pl.ismop.web.client.widgets.experiment;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Series.Type;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.widgets.experiment.IExperimentView.IExperimentPresenter;
import pl.ismop.web.client.widgets.experimentitem.ExperimentItemPresenter;

@Presenter(view = ExperimentView.class)
public class ExperimentPresenter extends BasePresenter<IExperimentView, MainEventBus> implements IExperimentPresenter {
	private DapController dapController;
	
	private ExperimentItemPresenter presenter;
	
	private Chart chart;

	@Inject
	public ExperimentPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowExperiment() {
		view.showModal(true);
		
		if(chart != null) {
			chart.removeAllSeries();
			chart.removeFromParent();
		}
		
		chart = new Chart()
				.setTitle(new ChartTitle().setText("Przebieg fali"), new ChartSubtitle());
		chart.getYAxis(0)
				.setAxisTitle(new AxisTitle().setText("Wysokość [m]"));
		chart.addSeries(chart.createSeries()
				.setName("Czas [h]")
				.setType(Type.LINE)
				.setYAxis(0));
		view.setChart(chart);
	}
	
	private native void push(double value, String timestamp, JavaScriptObject values) /*-{
		values.push({value: value, timestamp: timestamp});
	}-*/;
	
	private native JavaScriptObject showDygraphChart(String values, String yLabel, String title) /*-{
		return new $wnd.Dygraph($doc.getElementById('singlePlot'), values, {
			showRangeSelector: true,
			ylabel: yLabel,
			labelsDivStyles: {
				textAlign: 'right'
			},
			axisLabelWidth: 100,
			title: title,
			digitsAfterDecimal: 1,
			delimiter: '|'
		});
	}-*/;
	
	private String getDygraphValues(List<Measurement> measurements, String yLabel) {
		StringBuilder builder = new StringBuilder();
		builder.append("aa|").append(yLabel).append("\n");
		
		for(Measurement measurement : measurements) {
			DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
			Date date = format.parse(measurement.getTimestamp());
			date = new Date(date.getTime() - 7200000);
			builder.append(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(date))
					.append("|")
					.append(measurement.getValue())
					.append("\n");
		}
		
		return builder.toString();
	}

	@Override
	public void addChartPoint(int time, double height) {
		chart.getSeries()[0].addPoint(time, height);
	}

	@Override
	public void removeLastPoint() {
		int index = chart.getSeries()[0].getPoints().length - 1;
		
		if(index > -1) {
			chart.getSeries()[0].removePoint(chart.getSeries()[0].getPoints()[index]);
		}
	}
}