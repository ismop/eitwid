package pl.ismop.web.client.widgets.analysis.comparison;

import com.google.gwt.user.client.ui.IsWidget;

public interface IComparisonView extends IsWidget {
	interface IComparisonPresenter {
		
	}

	void setSlider(IsWidget slider);
}