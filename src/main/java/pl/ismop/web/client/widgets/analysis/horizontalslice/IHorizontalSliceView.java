package pl.ismop.web.client.widgets.analysis.horizontalslice;

import com.google.gwt.user.client.ui.IsWidget;

import javaslang.Tuple3;
import javaslang.Tuple4;
import javaslang.collection.Map;
import javaslang.collection.Seq;

public interface IHorizontalSliceView extends IsWidget {

	interface IHorizontalSlicePresenter {

	}

	void showLoadingState(boolean show);

	void drawCrosssection(Map<Double, Seq<Double>> legend, String parameterUnit,
			Map<String, Seq<? extends Map<Tuple4<Double, Double, Boolean, Boolean>,
			Tuple3<Integer, Integer, Integer>>>> locationsWithValues);

	void drawMuteSections(Seq<Seq<Seq<Double>>> coordinates);

	int getWidth();

	int getHeight();

	void drawScale(double scale, double panX);

	void init();

	void clear();

	String noMeasurementsMessage();

	boolean canRender();

	String cannotRenderMessages();
}
