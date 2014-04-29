package pl.ismop.web.client.widgets.summary;

import pl.ismop.web.client.widgets.summary.ILeveeSummaryView.ILeveeSummaryPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class LeveeSummaryView extends Composite implements ILeveeSummaryView, ReverseViewInterface<ILeveeSummaryPresenter> {
	private static LeveeSummaryViewUiBinder uiBinder = GWT.create(LeveeSummaryViewUiBinder.class);
	interface LeveeSummaryViewUiBinder extends UiBinder<Widget, LeveeSummaryView> {}
	
	private ILeveeSummaryPresenter presenter;

	@UiField LeveeSummaryViewMessages messages;
	@UiField DivElement modePanel;
	@UiField HeadingElement header;
	@UiField ParagraphElement mode;
	@UiField ParagraphElement threat;
	@UiField DivElement threatTimestamp;
	@UiField DivElement threatPanel;

	public LeveeSummaryView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("changeToNone")
	void changeToNone(ClickEvent event) {
		getPresenter().changeMode("none");
	}
	
	@UiHandler("changeToHeightened")
	void changeToHeightened(ClickEvent event) {
		getPresenter().changeMode("heightened");
	}
	
	@UiHandler("changeToSevere")
	void changeToSevere(ClickEvent event) {
		getPresenter().changeMode("severe");
	}

	@Override
	public void addModePanelStyle(String style) {
		modePanel.removeAttribute("class");
		modePanel.addClassName("panel");
		modePanel.addClassName(style);
	}

	@Override
	public void setHeader(String name) {
		header.setInnerText(name);
	}

	@Override
	public void setMode(String mode) {
		this.mode.setInnerText(mode);
	}

	@Override
	public void setThreatLastUpdated(String threatLastUpdated) {
		threatTimestamp.setInnerText(messages.threatLastUpdated(threatLastUpdated));
	}

	@Override
	public void setThreat(String threat) {
		this.threat.setInnerText(threat);
	}

	@Override
	public void addThreatPanelStyle(String style) {
		threatPanel.removeAttribute("class");
		threatPanel.addClassName("panel");
		threatPanel.addClassName(style);
	}

	@Override
	public void setPresenter(ILeveeSummaryPresenter presenter) {
		this.presenter = presenter;
		
	}

	@Override
	public ILeveeSummaryPresenter getPresenter() {
		return presenter;
	}
}