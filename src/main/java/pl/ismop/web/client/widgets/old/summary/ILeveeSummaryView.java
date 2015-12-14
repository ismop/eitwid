package pl.ismop.web.client.widgets.old.summary;

import com.google.gwt.user.client.ui.IsWidget;

public interface ILeveeSummaryView extends IsWidget {
	interface ILeveeSummaryPresenter {
		void changeMode(String mode);
	}

	void addModePanelStyle(String style);
	void setHeader(String name);
	void setMode(String mode);
	void setThreatLastUpdated(String formattedDate);
	void setThreat(String threat);
	void addThreatPanelStyle(String style);
}