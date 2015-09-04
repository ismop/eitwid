package pl.ismop.web.client.widgets.old.fibre;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.ui.IsWidget;

public interface IFibreView extends IsWidget {
	interface IFibrePresenter {
		void onSliderChanged(double value);
	}

	void showModal(boolean show);

	void setChart(Chart chart);
}