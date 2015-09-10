package pl.ismop.web.client.widgets.monitoring.fibre;

import java.util.Date;
import java.util.Random;

import com.google.common.eventbus.EventBus;
import com.google.gwt.core.client.GWT;
import org.gwtbootstrap3.client.ui.Label;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.PlotBand;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.XAxis;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.monitoring.fibre.IFibreView.IFibrePresenter;
import pl.ismop.web.client.widgets.slider.SliderPresenter;
import pl.ismop.web.client.widgets.slider.SliderView;

@Presenter(view = FibreView.class)
public class FibrePresenter extends BasePresenter<IFibreView, MainEventBus> implements IFibrePresenter {
	private Chart chart;
	private SliderPresenter slider;
	
	public void onShowFibrePanel() {
		view.showModal(true);

		if(chart != null) {
			chart.removeAllSeries();
			chart.removeFromParent();
		}
		
		chart = new Chart().
				setChartTitle(new ChartTitle()).
				setWidth(1100);
		chart.getXAxis().setPlotBands(createPlotBands(chart.getXAxis()));
		chart.getYAxis().
				setAxisTitle(new AxisTitle().
						setText("Temperarura [\u00B0C]"));
		chart.addSeries(chart.createSeries()
				.setName("Metr bieżacy światłowodu [m]")
				.setType(Type.SPLINE));

		Random random = new Random();

		for(int meter = 0; meter < 440; meter = meter + 15) {
			chart.getSeries()[0].addPoint(meter, random.nextInt(25) + 10);
		}

		Label status = new Label();
		status.setText("Testing string");

		if (slider == null) {
			slider = eventBus.addHandler(SliderPresenter.class);
			slider.setEventsListener(new SliderPresenter.Events() {
				@Override
				public void onDateChanged(Date currentDate) {
					GWT.log("Load fiber data from DAP using following date: " + currentDate);
				}
			});
			view.setSlider(slider.getView());
		}

		view.setChart(chart);
		view.setEmbenkment(status);
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
	public void onSliderChanged(Double value) {
		for(Point point : chart.getSeries()[0].getPoints()) {
			point.update(point.getX(), (point.getY().doubleValue() + value) % 36, false);
		}
		chart.redraw();
	}
}