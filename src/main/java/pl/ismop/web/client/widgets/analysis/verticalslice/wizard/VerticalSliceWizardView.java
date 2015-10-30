package pl.ismop.web.client.widgets.analysis.verticalslice.wizard;

import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class VerticalSliceWizardView extends Composite implements IVerticalSliceWizardView {
	private static VerticalSliceWizardViewUiBinder uiBinder = GWT.create(VerticalSliceWizardViewUiBinder.class);

	interface VerticalSliceWizardViewUiBinder extends UiBinder<Widget, VerticalSliceWizardView> {}

	@UiField
	Modal modal;
	
	public VerticalSliceWizardView() {
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