package pl.ismop.web.client.widgets.root;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class RootPanel extends Composite {
	private static RootPanelUiBinder uiBinder = GWT.create(RootPanelUiBinder.class);
	interface RootPanelUiBinder extends UiBinder<Widget, RootPanel> {}
	
	@UiField HTMLPanel mapPanel;
	@UiField HTMLPanel detailsPanel;

	public RootPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		mapPanel.getElement().setId("mapPanel");
		detailsPanel.getElement().setId("detailsPanel");
	}
}