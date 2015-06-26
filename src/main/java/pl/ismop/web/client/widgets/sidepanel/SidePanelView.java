package pl.ismop.web.client.widgets.sidepanel;

import org.gwtbootstrap3.client.ui.ListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SidePanelView extends Composite implements ISidePanelView {
	private static SidePanelViewUiBinder uiBinder = GWT.create(SidePanelViewUiBinder.class);
	interface SidePanelViewUiBinder extends UiBinder<Widget, SidePanelView> {}

	@UiField ListBox levees;
	
	public SidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void addLeveeValue(String leveeId, String leveeName) {
		levees.addItem(leveeName, leveeId);
	}
}