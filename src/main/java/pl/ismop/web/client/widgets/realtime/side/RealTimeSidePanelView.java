package pl.ismop.web.client.widgets.realtime.side;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RealTimeSidePanelView extends Composite implements IRealTimeSidePanelView {
	private static RealTimeSidePanelViewUiBinder uiBinder = GWT.create(RealTimeSidePanelViewUiBinder.class);

	interface RealTimeSidePanelViewUiBinder extends UiBinder<Widget, RealTimeSidePanelView> {}

	public RealTimeSidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}