package pl.ismop.web.client.widgets.root;

import org.gwtbootstrap3.client.ui.AnchorListItem;

import pl.ismop.web.client.widgets.root.IRootPanelView.IRootPresenter;

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
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class RootPanel extends Composite implements IRootPanelView, ReverseViewInterface<IRootPresenter> {
	private static RootPanelUiBinder uiBinder = GWT.create(RootPanelUiBinder.class);
	interface RootPanelUiBinder extends UiBinder<Widget, RootPanel> {}
	
	private IRootPresenter presenter;
	
	@UiField HTMLPanel mapPanel;
	@UiField FormPanel logoutForm;
	@UiField Hidden csrf;
	@UiField AnchorListItem levees;
	@UiField AnchorListItem sensors;

	public RootPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		mapPanel.getElement().setId("mapPanel");
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
	
	@UiHandler("levees")
	void showLevees(ClickEvent event) {
		getPresenter().onShowLevees(!levees.isActive());
		levees.setActive(!levees.isActive());
	}
	
	@UiHandler("sensors")
	void showSensors(ClickEvent event) {
		getPresenter().onShowSensors(!sensors.isActive());
		sensors.setActive(!sensors.isActive());
	}

	@Override
	public void setPresenter(IRootPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IRootPresenter getPresenter() {
		return presenter;
	}
}