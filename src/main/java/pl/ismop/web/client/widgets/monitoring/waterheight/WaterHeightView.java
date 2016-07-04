package pl.ismop.web.client.widgets.monitoring.waterheight;

import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class WaterHeightView extends Composite implements IWaterHeightView {

	private static WaterHeightUiBinder uiBinder = GWT.create(WaterHeightUiBinder.class);

	interface WaterHeightUiBinder extends UiBinder<Widget, WaterHeightView> {}
	
	@UiField
	Modal modal;
	
	public WaterHeightView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void showModal(boolean show) {
		modal.show();
	}
}
