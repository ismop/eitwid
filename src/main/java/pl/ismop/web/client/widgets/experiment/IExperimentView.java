package pl.ismop.web.client.widgets.experiment;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.ui.IsWidget;

public interface IExperimentView extends IsWidget {
	interface IExperimentPresenter {
		void addChartPoint(int time, double height);

		void removeLastPoint();
	}

	String getMainTitle();
	
	void showFirstWave();
	
	void showModal(boolean show);

	void setChart(Chart chart);
}