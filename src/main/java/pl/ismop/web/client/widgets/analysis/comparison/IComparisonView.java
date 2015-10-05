package pl.ismop.web.client.widgets.analysis.comparison;

import com.google.gwt.user.client.ui.IsWidget;
import pl.ismop.web.client.widgets.common.panel.IPanelView;

public interface IComparisonView extends IsWidget {
	interface IComparisonPresenter {
		void addChart();
		void addHorizontalCS();
		void addVerticalCS();
	}

	void setSlider(IsWidget slider);
	void addPanel(IPanelView panel);
	void removePanel(IPanelView panel);
	void movePanelUp(IPanelView view);
	void movePanelDown(IPanelView view);

}