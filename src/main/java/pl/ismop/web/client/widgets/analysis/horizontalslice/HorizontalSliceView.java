package pl.ismop.web.client.widgets.analysis.horizontalslice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalSliceView extends Composite implements IHorizontalSliceView {
	private static HorizontalSliceViewUiBinder uiBinder = GWT.create(HorizontalSliceViewUiBinder.class);

	interface HorizontalSliceViewUiBinder extends UiBinder<Widget, HorizontalSliceView> {}

	public HorizontalSliceView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}