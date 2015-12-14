package pl.ismop.web.client.widgets.old.summary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.old.summary.ILeveeSummaryView.ILeveeSummaryPresenter;

public class LeveeSummaryView extends Composite implements ILeveeSummaryView, ReverseViewInterface<ILeveeSummaryPresenter> {
	private static LeveeSummaryViewUiBinder uiBinder = GWT.create(LeveeSummaryViewUiBinder.class);
	interface LeveeSummaryViewUiBinder extends UiBinder<Widget, LeveeSummaryView> {}
	
	private ILeveeSummaryPresenter presenter;

	@UiField LeveeSummaryViewMessages messages;
	@UiField DivElement header;
	@UiField SpanElement mode;
	@UiField SpanElement threat;
	@UiField DivElement threatTimestamp;

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
		mode.removeAttribute("class");
		mode.addClassName(style);
	}

	@Override
	public void setHeader(String name) {
		header.setInnerText(name);
	}

	@Override
	public void setMode(String mode) {
		this.mode.setInnerText(translateSystemMode(mode));
	}

	@Override
	public void setThreatLastUpdated(String threatLastUpdated) {
		threatTimestamp.setInnerText(messages.threatLastUpdated(threatLastUpdated));
	}

	@Override
	public void setThreat(String threat) {
		this.threat.setInnerText(translateThreatLevel(threat));
	}

	@Override
	public void addThreatPanelStyle(String style) {
		threat.removeAttribute("class");
		threat.addClassName(style);
	}

	@Override
	public void setPresenter(ILeveeSummaryPresenter presenter) {
		this.presenter = presenter;
		
	}

	@Override
	public ILeveeSummaryPresenter getPresenter() {
		return presenter;
	}
	
	private String translateSystemMode(String mode) {
		switch(mode) {
			case "none":
				return messages.stantbyModeLabel();
			case "heightened":
				return messages.alertModeLabel();
			case "severe":
				return messages.threatModeLabel();
			default:
				return messages.modeUnknown(mode);
		}
	}
	
	private String translateThreatLevel(String threatLevel) {
		switch(threatLevel) {
			case "none":
				return messages.threatLevelNone();
			case "heightened":
				return messages.threatLevelElevated();
			case "severe":
				return messages.threatLevelSevere();
			default:
				return messages.threatLevelUnknown();
		}
	}
}