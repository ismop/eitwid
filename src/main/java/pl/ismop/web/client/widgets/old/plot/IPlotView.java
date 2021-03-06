package pl.ismop.web.client.widgets.old.plot;

import com.google.gwt.user.client.ui.IsWidget;

public interface IPlotView extends IsWidget {
	interface IPlotPresenter {
		
	}

	void showMessageLabel(boolean show);

	void setNoParamtersMessage();

	void setNoContextsMessage();

	void setNoTimelinesMessage();

	void setNoMeasurementsMessage();

	void setPlot(IsWidget widget);

	void showBusyPanel(boolean busy);
}