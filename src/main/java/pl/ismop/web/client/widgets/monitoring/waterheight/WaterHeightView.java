package pl.ismop.web.client.widgets.monitoring.waterheight;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.monitoring.waterheight.IWaterHeightView.IWaterHeightPresenter;

public class WaterHeightView extends Composite implements IWaterHeightView, ReverseViewInterface<IWaterHeightPresenter> {

	private static WaterHeightUiBinder uiBinder = GWT.create(WaterHeightUiBinder.class);

	interface WaterHeightUiBinder extends UiBinder<Widget, WaterHeightView> {}
	
	@UiField
	Modal modal;
	
	@UiField
	FlowPanel waterHeightChart;
	
	private IWaterHeightPresenter presenter;
	
	public WaterHeightView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void showModal(boolean show) {
		modal.show();
	}
	
	@UiHandler("modal")
	void onPopupReady(ModalShownEvent event) {
		getPresenter().onModalReady();
	}

	@Override
	public void setPresenter(IWaterHeightPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IWaterHeightPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setChart(IsWidget chart) {
		waterHeightChart.clear();
		waterHeightChart.add(chart);
	}
	
	@Override
	public int getChartHeight() {
		return waterHeightChart.getOffsetHeight();
	}
}
