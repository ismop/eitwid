package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.user.client.ui.IsWidget;

import java.util.Date;

public interface IFibreView extends IsWidget {
	FibreMessages getMessages();

	interface IFibrePresenter {
		void onSliderChanged(Date selectedTime);

		void onModalReady();
	}

	void showModal(boolean show);

	void addElementToLeftPanel(IsWidget widget);

	void addElementToRightPanel(IsWidget widget);

	int getLeftPanelWidth();
}