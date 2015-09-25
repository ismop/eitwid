package pl.ismop.web.client.widgets.analysis.sidepanel;

import com.google.gwt.user.client.ui.IsWidget;

public interface IAnalysisSidePanel extends IsWidget {
	int getWaterWavePanelWidth();

	int getWaterWavePanelHeight();

	interface IAnalysisSidePanelPresenter {

	}

	void setWaterWavePanel(IsWidget widget);
	void setMinimap(IsWidget widget);
	void addAction(IsWidget widget);
}
