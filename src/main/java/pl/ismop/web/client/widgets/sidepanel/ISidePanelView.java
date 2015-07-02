package pl.ismop.web.client.widgets.sidepanel;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISidePanelView extends IsWidget {
	interface ISidePanelPresenter {
		
	}

	void addLeveeValue(String leveeId, String leveeName);

	void setLeveeBusyState(boolean busy);

	void showLeveeList(boolean show);

	void showNoLeveesLabel(boolean show);

	void removeSummaryView();

	void addLeveeSummary(IsWidget view);

	void showSectionPanel(boolean show);

	void setSectionBusyState(boolean busy);

	void showNoSectionsLabel(boolean show);

	void addSectionValue(String sectionId, String sectionName);

	void showSectionList(boolean show);
}