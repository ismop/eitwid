package pl.ismop.web.client.widgets.sidepanel;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISidePanelView extends IsWidget {
	interface ISidePanelPresenter {
		void onSectionChanged(String sectionId);

		void onProfileChanged(String profileId);

		void onDeviceChanged(List<String> deviceIds);

		void onAddExperiment();
	}

	void addLeveeValue(String leveeId, String leveeName);

	void setLeveeBusyState(boolean busy);

	void showLeveeList(boolean show);

	void showNoLeveesLabel(boolean show);

	void removeSummaryView();

	void setLeveeSummary(IsWidget view);

	void showSectionPanel(boolean show);

	void setSectionBusyState(boolean busy);

	void showNoSectionsLabel(boolean show);

	void addSectionValue(String sectionId, String sectionName);

	void showSectionList(boolean show);

	void removeSectionView();

	void setSectionView(IsWidget view);

	String getPickSectionLabel();

	void setProfileBusyState(boolean busy);

	void showNoProfilesLabel(boolean show);

	void showProfileList(boolean show);

	void addProfileValue(String profileId, String profileName);

	String getPickProfileLabel();

	void showProfilePanel(boolean show);

	void clearProfileValues();

	void setSelectedSection(String sectionId);

	String getSelectedLeveeId();

	void showDevicePanel(boolean show);

	void setDeviceBusyState(boolean busy);

	void showDeviceList(boolean show);

	void showNoDevicesLabel(boolean show);

	void addDeviceValue(String deviceId, String deviceName);

	void showPlotContainer(boolean show);

	void setPlotView(IsWidget plot);

	void clearDeviceValues();

	void setSelectedProfile(String profileId);

	void setSelectedDevices(List<String> deviceIds);
}