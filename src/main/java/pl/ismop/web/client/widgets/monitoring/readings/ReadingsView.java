package pl.ismop.web.client.widgets.monitoring.readings;

import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ReadingsView extends Composite implements IReadingsView {
	private static ReadingsViewUiBinder uiBinder = GWT.create(ReadingsViewUiBinder.class);

	interface ReadingsViewUiBinder extends UiBinder<Widget, ReadingsView> {}

	@UiField
	Modal modal;
	
	public ReadingsView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showModal(boolean show) {
		if(show) {
			modal.show();
		} else {
			modal.hide();
		}
	}
}