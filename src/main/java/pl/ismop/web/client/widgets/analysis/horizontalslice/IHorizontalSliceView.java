package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceView extends IsWidget {
	
	interface IHorizontalSlicePresenter {
		
	}

	void showLoadingState(boolean show);

	void drawCrosssection(Map<Double, List<Double>> legend, String parameterUnit,
			Map<List<List<Double>>, Map<List<Double>, List<Double>>> locationsWithValues);

	void drawMuteSections(List<List<List<Double>>> coordinates);

	int getWidth();

	int getHeight();

	void drawScale(double scale, double panX);

	void init();

	void clear();

	String noMeasurementsMessage();

	boolean canRender();

	String cannotRenderMessages();
}