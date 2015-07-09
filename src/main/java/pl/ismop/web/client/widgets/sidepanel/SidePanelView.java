package pl.ismop.web.client.widgets.sidepanel;

import org.gwtbootstrap3.client.ui.ListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.sidepanel.ISidePanelView.ISidePanelPresenter;

public class SidePanelView extends Composite implements ISidePanelView, ReverseViewInterface<ISidePanelPresenter> {
	private static SidePanelViewUiBinder uiBinder = GWT.create(SidePanelViewUiBinder.class);
	interface SidePanelViewUiBinder extends UiBinder<Widget, SidePanelView> {}
	
	private ISidePanelPresenter presenter;

	@UiField
	SidePanelMessages messages;
	
	@UiField
	ListBox levees, sections, profiles;

	@UiField
	FlowPanel leveeBusyPanel, leveePanel, sectionPanel, sectionBusyPanel, sectionDetails, profilePanel, profileBusyPanel;
	
	@UiField
	Label noLeveesLabel, noSectionsLabel, noProfilesLabel;
	
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
}