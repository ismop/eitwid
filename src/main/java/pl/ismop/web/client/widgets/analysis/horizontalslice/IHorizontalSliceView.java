package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceView extends IsWidget {
	interface IHorizontalSlicePresenter {
		
	}

	void showLoadingState(boolean show);

	void drawCrosssection(String parameterUnit, double minValue, double maxValue, Map<List<List<Double>>, Map<List<Double>, Double>> locationsWithValues);

	void drawMuteSections(List<List<List<Double>>> coordinates);

	int getWidth();

	int getHeight();

	void drawScale(double scale, double panX);

	void showNoMeasurementsMessage();

	void init();

	void clear();
}