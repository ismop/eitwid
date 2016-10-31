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
import org.gwtbootstrap3.client.ui.Button;
import pl.ismop.web.client.widgets.common.panel.IPanelView;

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

	@UiField
	Button addChart;

	@UiField
	Button addHorizontalCS;

	@UiField
	Button addVerticalCS;
	
	@UiField
	Button addThreadAssesment;

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

	@UiHandler("addThreadAssesment")
	void addThreadAssesment(ClickEvent click) {
		getPresenter().addThreadAssesment();
	}
	
	@Override
	public void setSlider(IsWidget slider) {
		sliderPanel.add(slider);
	}

	@Override
	public void addPanel(IPanelView panel) {
		panels.add(panel);
		updateFirstAndLast();
	}

	@Override
	public void removePanel(IPanelView panel) {
		panels.remove(panel);
		updateFirstAndLast();
	}

	@Override
	public void movePanelUp(IPanelView panel) {
		int index = panels.getWidgetIndex(panel);

		changePossition(panel, index > 0 ? index - 1 : index);
	}

	@Override
	public void movePanelDown(IPanelView panel) {
		int index = panels.getWidgetIndex(panel);

		changePossition(panel, index < panels.getWidgetCount() -1 ? index + 1 : index);
	}

	private void changePossition(IPanelView panel, int newIndex) {
		panels.remove(panel);
		panels.insert(panel, newIndex);

		updateFirstAndLast();
	}

	private void updateFirstAndLast() {
		int size = panels.getWidgetCount();

		for(int i = 0; i < size; i++) {
			IPanelView panel = (IPanelView) panels.getWidget(i);
			panel.setFirst(i == 0);
			panel.setLast(i == size - 1);
		}
	}

	@Override
	public void setPresenter(IComparisonPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IComparisonPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setActionsEnabled(boolean enabled) {
		addChart.setEnabled(enabled);
		addHorizontalCS.setEnabled(enabled);
		addVerticalCS.setEnabled(enabled);
		addThreadAssesment.setEnabled(enabled);
	}
}