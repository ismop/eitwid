package pl.ismop.web.client.widgets.monitoring.sidepanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.ui.*;
import pl.ismop.web.client.widgets.monitoring.sidepanel.IMonitoringSidePanel.IMonitoringSidePanelPresenter;

public class MonitoringSidePanelView extends Composite implements IMonitoringSidePanel, ReverseViewInterface<IMonitoringSidePanelPresenter> {
	private static MonitoringSidePanelViewUiBinder uiBinder = GWT.create(MonitoringSidePanelViewUiBinder.class);

	interface MonitoringSidePanelViewUiBinder extends UiBinder<Widget, MonitoringSidePanelView> {}
	
	private IMonitoringSidePanelPresenter presenter;

	@UiField
	MonitoringSidePanelMessages messages;
	
	@UiField
	FormControlStatic leveeName;
	
	@UiField
	ListBox leveeList;
	
	@UiField
	FlowPanel leveeProgress, metadataEntries, metadataPanel, chart;
	
	@UiField
	Button expandChart, clearChart;
	
	@UiField
	ButtonGroup chartButtons;
	
	public MonitoringSidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("showWeather")
	void showWeather(ClickEvent event) {
		getPresenter().handleShowWeatherClick();
	}
	
	@UiHandler("showFibre")
	void showFibre(ClickEvent event) {
		getPresenter().handleShowFibreClick();
	}
	
	@UiHandler("showWaterHigh")
	void showFibreHigh(ClickEvent event) {
		getPresenter().handleShowFibreHighClick();
	}
	
	@UiHandler("expandChart")
	void expandChart(ClickEvent event) {
		getPresenter().onExpandChart();
	}
	
	@UiHandler("clearChart")
	void clearChart(ClickEvent event) {
		getPresenter().onClearChart();
	}
	
	@Override
	public void setPresenter(IMonitoringSidePanelPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IMonitoringSidePanelPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void showLeveeName(boolean show) {
		leveeName.setVisible(show);
	}

	@Override
	public void showLeveeList(boolean show) {
		leveeList.setVisible(show);
	}

	@Override
	public void showLeveeProgress(boolean show) {
		leveeProgress.setVisible(show);
	}

	@Override
	public void showNoLeveesMessage() {
		leveeName.setText(messages.noLeveesLabel());
	}

	@Override
	public void addLeveeOption(String leveeid, String leveeName) {
		leveeList.addItem(leveeName, leveeid);
	}

	@Override
	public void setLeveeName(String leveeName) {
		this.leveeName.setText(leveeName);
	}

	@Override
	public String getNameLabel() {
		return messages.nameLabel();
	}

	@Override
	public String getInternalIdLabel() {
		return messages.internalIdLabel();
	}

	@Override
	public void addMetadata(String label, String value) {
		DescriptionTitle title = new DescriptionTitle();
		title.setText(label);
		
		DescriptionData data = new DescriptionData();
		data.setText(value);
		
		Description description = new Description();
		description.add(title);
		description.add(data);
		metadataEntries.add(description);
	}

	@Override
	public void clearMetadata() {
		metadataEntries.clear();
	}

	@Override
	public void setChart(IsWidget view) {
		chart.add(view);
	}

	@Override
	public int getChartHeight() {
		//offset height minus padding
		return chart.getOffsetHeight() - 40;
	}

	@Override
	public void showChartButtons(boolean show) {
		chartButtons.setVisible(show);
	}

	@Override
	public String getProfileTypeLabel() {
		return messages.profileTypeLabel();
	}

	@Override
	public String getTypeLabel() {
		return messages.typeLabel();
	}

	@Override
	public String getSectionTypeLabel() {
		return messages.sectionTypeLabel();
	}

	@Override
	public String getDeviceTypeLabel() {
		return messages.deviceTypeLabel();
	}

	@Override
	public String getDeviceAggregateTypeLabel() {
		return messages.deviceAggregateTypeLabel();
	}

	@Override
	public String getAggregateContentsLabel() {
		return messages.aggregateContents();
	}

	@Override
	public String getNoMeasurementsForDeviceMessage() {
		return messages.noMeasurementsForDevice();
	}

	@Override
	public MonitoringSidePanelMessages getMessages() {
		return messages;
	}

	@Override
	public void showChart(boolean show) {
		chart.setVisible(show);
	}
}