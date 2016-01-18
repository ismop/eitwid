package pl.ismop.web.client.widgets.root;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.NavbarLink;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;

public class RootPanel extends Composite implements IRootPanelView, ReverseViewInterface<IRootPresenter> {
	private static RootPanelUiBinder uiBinder = GWT.create(RootPanelUiBinder.class);
	interface RootPanelUiBinder extends UiBinder<Widget, RootPanel> {}
	
	private IRootPresenter presenter;
	
	@UiField
	RootPanelMessages messages;
	
	@UiField
	HTMLPanel mainPanel, sidePanel;
	
	@UiField
	FormPanel logoutForm;
	
	@UiField
	Hidden csrf;
	
	@UiField
	AnchorListItem monitoring, analysis, realTime;
	
	@UiField
	NavbarLink brokenDevices;

	public RootPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		csrf.setName(DOM.getElementById("csrfParameterName").getAttribute("content"));
		csrf.setValue(DOM.getElementById("csrfToken").getAttribute("content"));
	}
	
	@UiHandler("logout")
	void logout(ClickEvent event) {
		event.preventDefault();
		logoutForm.submit();
	}
	
	@UiHandler("logoutForm")
	void afterLogout(SubmitCompleteEvent event) {
		Window.Location.assign("/login?logout");
	}
	
	@UiHandler("analysis")
	void analysisClicked(ClickEvent event) {
		getPresenter().onAnalysisViewOption();
	}
	
	@UiHandler("monitoring")
	void monitoringClicke(ClickEvent event) {
		getPresenter().onMonitoringViewOption();
	}
	
	@UiHandler("realTime")
	void realTimeClicke(ClickEvent event) {
		getPresenter().onRealTimeViewOption();
	}
	
	@UiHandler("brokenDevices")
	void brokenDevices(ClickEvent event) {
		getPresenter().onBrokenDevicesClicked();
	}

	@Override
	public void setPresenter(IRootPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IRootPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void markAnalysisOption(boolean mark) {
		analysis.setActive(mark);
	}

	@Override
	public void markMonitoringOption(boolean mark) {
		monitoring.setActive(mark);
	}

	@Override
	public void markRealTimeOption(boolean mark) {
		realTime.setActive(mark);
	}

	@Override
	public void clearPanels() {
		mainPanel.clear();
		sidePanel.clear();
	}

	@Override
	public void setSidePanelWidget(IsWidget view) {
		sidePanel.add(view);
	}

	@Override
	public void setMainPanelWidget(IsWidget view) {
		mainPanel.add(view);
	}

	@Override
	public void setBrokenDevicesLinkLabel(int numberOfBrokenDevices) {
		brokenDevices.setText(messages.brokenDevicesLabel(numberOfBrokenDevices));
	}

	@Override
	public void showBrokenDevicesLink(boolean show, boolean alert) {
		brokenDevices.setVisible(show);
		
		if(alert) {
			brokenDevices.getElement().getStyle().setColor("#a94442");
		} else {
			brokenDevices.getElement().getStyle().clearColor();
		}
	}

	@Override
	public void showDetails(List<String> brokenParameters) {
		String parameters = "<ul>";
		
		for(String brokenParameter : brokenParameters) {
			parameters += "<li>" + brokenParameter + "</li>";
		}
		
		parameters += "</ul>";
		
		String message = messages.brokenParametersDetails(parameters);
		Bootbox.alert(message);
	}
}