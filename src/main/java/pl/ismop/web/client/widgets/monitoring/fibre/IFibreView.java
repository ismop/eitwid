package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.user.client.ui.IsWidget;

import java.util.Date;

public interface IFibreView extends IsWidget {
	interface IFibrePresenter {
		void onSliderChanged(Date selectedTime);
	}

	void showModal(boolean show);

	void setChart(IsWidget chart);

	void setEmbenkment(IsWidget label);

	void setSlider(IsWidget slider);
}