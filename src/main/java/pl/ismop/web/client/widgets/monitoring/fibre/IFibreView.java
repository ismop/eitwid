package pl.ismop.web.client.widgets.monitoring.fibre;

import org.gwtbootstrap3.client.ui.Label;
import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.ui.IsWidget;
import pl.ismop.web.client.widgets.slider.SliderView;

public interface IFibreView extends IsWidget {
	interface IFibrePresenter {
		void onSliderChanged(Double value);
	}

	void showModal(boolean show);

	void setChart(Chart chart);

	void setEmbenkment(Label label);

	void setSlider(IsWidget slider);
}