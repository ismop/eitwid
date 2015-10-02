package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalSliceWizardView extends Composite implements IHorizontalSliceWizardView {
	private static HorizontalSliceWizardViewUiBinder uiBinder = GWT.create(HorizontalSliceWizardViewUiBinder.class);

	interface HorizontalSliceWizardViewUiBinder extends UiBinder<Widget, HorizontalSliceWizardView> {}
	
	@UiField
	Modal modal;

	public HorizontalSliceWizardView() {
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