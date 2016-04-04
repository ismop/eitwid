package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IVerticalSliceView extends IsWidget {
	interface IVerticalSlicePresenter {
		
	}

	void showLoadingState(boolean show);

	void clear();

	void init();

	void drawCrosssection(String parameterUnit, boolean leftBank,
			Map<Double, List<Double>> profileAndDevicePositionsWithValues,
			Map<Double, List<Double>> legend);

	String noMeasurementsMessage();

	String cannotRenderMessages();

	boolean canRender();
}