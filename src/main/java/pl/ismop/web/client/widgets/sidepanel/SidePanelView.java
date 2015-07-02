package pl.ismop.web.client.widgets.sidepanel;

import org.gwtbootstrap3.client.ui.ListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SidePanelView extends Composite implements ISidePanelView {
	private static SidePanelViewUiBinder uiBinder = GWT.create(SidePanelViewUiBinder.class);
	interface SidePanelViewUiBinder extends UiBinder<Widget, SidePanelView> {}

	@UiField ListBox levees;
	@UiField FlowPanel leveeBusyPanel;
	@UiField Label noLeveesLabel;
	@UiField FlowPanel summaryPanel;
	@UiField FlowPanel sectionPanel;
	@UiField FlowPanel sectionBusyPanel;
	@UiField Label noSectionsLabel;
	@UiField ListBox sections;
	@UiField FlowPanel sectionDetails;
	
	public SidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
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
		summaryPanel.clear();
	}

	@Override
	public void setLeveeSummary(IsWidget view) {
		summaryPanel.add(view);
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
}