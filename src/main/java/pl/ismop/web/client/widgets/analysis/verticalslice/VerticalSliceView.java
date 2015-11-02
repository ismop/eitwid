package pl.ismop.web.client.widgets.analysis.verticalslice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class VerticalSliceView extends Composite implements IVerticalSliceView {
	private static VerticalSliceViewUiBinder uiBinder = GWT.create(VerticalSliceViewUiBinder.class);

	interface VerticalSliceViewUiBinder extends UiBinder<Widget, VerticalSliceView> {}

	public VerticalSliceView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}