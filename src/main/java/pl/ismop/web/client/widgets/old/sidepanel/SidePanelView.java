package pl.ismop.web.client.widgets.old.sidepanel;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Description;
import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.client.ui.DescriptionTitle;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.old.sidepanel.ISidePanelView.ISidePanelPresenter;

public class SidePanelView extends Composite implements ISidePanelView, ReverseViewInterface<ISidePanelPresenter> {
	private static SidePanelViewUiBinder uiBinder = GWT.create(SidePanelViewUiBinder.class);
	interface SidePanelViewUiBinder extends UiBinder<Widget, SidePanelView> {}
	
	private ISidePanelPresenter presenter;

	@UiField
	SidePanelMessages messages;
	
	@UiField
	ListBox levees, sections, profiles;
	
	@UiField
	MultipleSelect sensors;

	@UiField
	FlowPanel leveeBusyPanel, leveePanel, sectionPanel, sectionBusyPanel, sectionDetails, profilePanel, profileBusyPanel, sensorPanel, sensorBusyPanel,
			plotContainer, sensorListPanel, experimentPlanDetails, experimentPlanChart;
	
	@UiField
	Label noLeveesLabel, noSectionsLabel, noProfilesLabel, noSensorsLabel, noExperimentPlansLabel;
	
	public SidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("sections")
	void sectionPicked(ChangeEvent event) {
		getPresenter().onSectionChanged(sections.getSelectedValue());
	}
	
	@UiHandler("profiles")
	void profilePicked(ChangeEvent event) {
		getPresenter().onProfileChanged(profiles.getSelectedValue());
	}
	
	@UiHandler("sensors")
	void sensorPicked(ChangeEvent event) {
		List<String> result = new ArrayList<String>();
		
		for (Option option : sensors.getSelectedItems()) {
			result.add(option.getValue());
		}
		
		getPresenter().onDeviceChanged(result);
	}
	
	@UiHandler("addExperiment")
	void addExperiment(ClickEvent event) {
		getPresenter().onAddExperiment();
	}

	@Override
	public void addLeveeValue(String leveeId, String leveeName) {
		levees.addItem(leveeName, leveeId);
	}

	@Override
	public void setLeveeBusyState(boolean busy) {
		leveeBusyPanel.setVisible(busy);
	}

	@Override
	public void showLeveeList(boolean show) {
		levees.setVisible(show);
	}

	@Override
	public void showNoLeveesLabel(boolean show) {
		noLeveesLabel.setVisible(show);
	}

	@Override
	public void removeSummaryView() {
		leveePanel.clear();
	}

	@Override
	public void setLeveeSummary(IsWidget view) {
		leveePanel.add(view);
	}

	@Override
	public void showSectionPanel(boolean show) {
		sectionPanel.setVisible(show);
	}

	@Override
	public void setSectionBusyState(boolean busy) {
		sectionBusyPanel.setVisible(busy);
	}

	@Override
	public void showNoSectionsLabel(boolean show) {
		noSectionsLabel.setVisible(show);
	}

	@Override
	public void addSectionValue(String sectionId, String sectionName) {
		sections.addItem(sectionName, sectionId);
	}

	@Override
	public void showSectionList(boolean show) {
		sections.setVisible(show);
	}

	@Override
	public void removeSectionView() {
		sectionDetails.clear();
	}

	@Override
	public void setSectionView(IsWidget view) {
		sectionDetails.add(view);
	}

	@Override
	public String getPickSectionLabel() {
		return messages.pickSectionLabel();
	}

	@Override
	public void setPresenter(ISidePanelPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public ISidePanelPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setProfileBusyState(boolean busy) {
		profileBusyPanel.setVisible(busy);
	}

	@Override
	public void showNoProfilesLabel(boolean show) {
		noProfilesLabel.setVisible(show);
	}

	@Override
	public void showProfileList(boolean show) {
		profiles.setVisible(show);
	}

	@Override
	public void addProfileValue(String profileId, String profileName) {
		profiles.addItem(profileName, profileId);
	}

	@Override
	public String getPickProfileLabel() {
		return messages.pickProfileLabel();
	}

	@Override
	public void showProfilePanel(boolean show) {
		profilePanel.setVisible(show);
	}

	@Override
	public void clearProfileValues() {
		profiles.clear();
	}

	@Override
	public void setSelectedSection(String sectionId) {
		for(int i = 0; i < sections.getItemCount(); i++) {
			if(sectionId.equals(sections.getValue(i))) {
				sections.setSelectedIndex(i);
				
				break;
			}
		}
	}

	@Override
	public String getSelectedLeveeId() {
		return levees.getSelectedValue();
	}

	@Override
	public void showDevicePanel(boolean show) {
		sensorPanel.setVisible(show);
	}

	@Override
	public void setDeviceBusyState(boolean busy) {
		sensorBusyPanel.setVisible(busy);
	}

	@Override
	public void showDeviceList(boolean show) {
		sensorListPanel.setVisible(show);
	}

	@Override
	public void showNoDevicesLabel(boolean show) {
		noSensorsLabel.setVisible(show);
	}

	@Override
	public void addDeviceValue(String sensorId, String sensorName) {
		Option option = new Option();
		option.setValue(sensorId);
		option.setText(sensorName);
		sensors.add(option);
		sensors.refresh();
	}

	@Override
	public void showPlotContainer(boolean show) {
		plotContainer.setVisible(show);
	}

	@Override
	public void setPlotView(IsWidget plot) {
		plotContainer.add(plot);
	}

	@Override
	public void clearDeviceValues() {
		sensors.clear();
	}

	@Override
	public void setSelectedProfile(String profileId) {
		for(int i = 0; i < profiles.getItemCount(); i++) {
			if(profileId.equals(profiles.getValue(i))) {
				profiles.setSelectedIndex(i);
				
				break;
			}
		}
	}

	@Override
	public void setSelectedDevices(List<String> deviceIds) {
		sensors.setValue(deviceIds);
	}

	@Override
	public HasVisibility getNoExperimentPlansVisibility() {
		return noExperimentPlansLabel;
	}

	@Override
	public void setExperimentPlanName(String name) {
		experimentPlanDetails.add(createDescription(messages.getExperimentPlanNameLabel(), name));
	}

	@Override
	public void setExperimentPlanStartDate(String startDate) {
		experimentPlanDetails.add(createDescription(messages.getExperimentPlanNameStartDate(), startDate));
	}

	@Override
	public void setExperimentPlanMargin(String margin) {
		experimentPlanDetails.add(createDescription(messages.getExperimentPlanNameDeviation(), margin));
	}

	private Description createDescription(String titleValue, String dataValue) {
		Description description = new Description();
		description.setHorizontal(true);
		
		DescriptionTitle title = new DescriptionTitle();
		title.setText(titleValue);
		description.add(title);
		
		DescriptionData data = new DescriptionData();
		data.setText(dataValue);
		description.add(data);
		
		return description;
	}

	@Override
	public void setExperimentPlanChart(Chart chart) {
		experimentPlanChart.add(chart);
	}
}