package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSlicePresenter.Borehole;

public interface IVerticalSliceView extends IsWidget {
	interface IVerticalSlicePresenter {

	}

	void showLoadingState(boolean show);

	void clear();

	void init();

	void drawCrosssection(String parameterUnit, boolean leftBank, List<Borehole> boreholes,
			Map<Double, List<Double>> legend);

	String noMeasurementsMessage();

	String cannotRenderMessage();

	boolean canRender();
}