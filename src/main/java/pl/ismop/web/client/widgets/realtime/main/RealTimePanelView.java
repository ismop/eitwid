package pl.ismop.web.client.widgets.realtime.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RealTimePanelView extends Composite implements IRealTimePanelView {
	private static RealTimePanelViewUiBinder uiBinder = GWT.create(RealTimePanelViewUiBinder.class);

	interface RealTimePanelViewUiBinder extends UiBinder<Widget, RealTimePanelView> {}

	public RealTimePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}