package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IVerticalSliceView extends IsWidget {
	interface IVerticalSlicePresenter {
		
	}

	void showLoadingState(boolean show);

	void clear();

	void init();

	void drawCrosssection(String parameterUnit, double minValue, double maxValue, Map<Double, Double> profileAndDevicePositionsWithValues);

	void showNoMeasurementsMessage();
}