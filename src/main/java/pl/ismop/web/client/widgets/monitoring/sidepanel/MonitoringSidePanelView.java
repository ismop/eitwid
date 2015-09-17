package pl.ismop.web.client.widgets.monitoring.sidepanel;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Description;
import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.client.ui.DescriptionTitle;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.ListBox;
import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

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
	Button expand;
	
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
	public void setChart(Chart chart) {
		this.chart.clear();
		this.chart.add(chart);
	}

	@Override
	public Number getChartContainerHeight() {
		return chart.getOffsetHeight() - 40;
	}

	@Override
	public void showChartExpandButton(boolean show) {
		expand.setVisible(show);
	}
}