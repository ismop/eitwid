package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceView extends IsWidget {
	interface IHorizontalSlicePresenter {
		
	}

	void showLoadingState(boolean show);

	void drawCrosssection();

	void drawMuteSections(List<List<List<Double>>> coordinates);
}