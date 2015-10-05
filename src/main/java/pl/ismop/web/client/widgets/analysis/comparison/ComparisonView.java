package pl.ismop.web.client.widgets.analysis.comparison;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import pl.ismop.web.client.widgets.common.panel.IPanelView;
import pl.ismop.web.client.widgets.slider.SliderPresenter;

public class ComparisonView extends Composite implements IComparisonView, ReverseViewInterface<IComparisonView.IComparisonPresenter> {
	private static ComparisonViewUiBinder uiBinder = GWT.create(ComparisonViewUiBinder.class);
	private IComparisonPresenter presenter;

	interface ComparisonViewUiBinder extends UiBinder<Widget, ComparisonView> {}

	public ComparisonView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	FlowPanel sliderPanel;

	@UiField
	FlowPanel panels;

	@UiHandler("addChart")
	void addChart(ClickEvent click) {
		getPresenter().addChart();
	}

	@UiHandler("addHorizontalCS")
	void addHorizontalCS(ClickEvent click) {
		getPresenter().addHorizontalCS();
	}

	@UiHandler("addVerticalCS")
	void addVerticalCS(ClickEvent click) {
		getPresenter().addVerticalCS();
	}

	@Override
	public void setSlider(IsWidget slider) {
		sliderPanel.add(slider);
	}

	@Override
	public void addPanel(IPanelView panel) {
		panels.add(panel);
	}

	@Override
	public void removePanel(IPanelView panel) {
		panels.remove(panel);
	}

	@Override
	public void setPresenter(IComparisonPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IComparisonPresenter getPresenter() {
		return presenter;
	}
}