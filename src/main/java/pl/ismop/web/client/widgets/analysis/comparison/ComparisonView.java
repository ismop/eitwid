package pl.ismop.web.client.widgets.analysis.comparison;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import pl.ismop.web.client.widgets.slider.SliderPresenter;

public class ComparisonView extends Composite implements IComparisonView {
	private static ComparisonViewUiBinder uiBinder = GWT.create(ComparisonViewUiBinder.class);

	interface ComparisonViewUiBinder extends UiBinder<Widget, ComparisonView> {}

	public ComparisonView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	FlowPanel sliderPanel;

	@Override
	public void setSlider(IsWidget slider) {
		sliderPanel.add(slider);
	}
}