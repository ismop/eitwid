package pl.ismop.web.client.widgets.old.fibre;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideEvent;
import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.old.fibre.IFibreView.IFibrePresenter;

public class FibreView extends Composite implements IFibreView, ReverseViewInterface<IFibrePresenter> {
	private static FibreViewUiBinder uiBinder = GWT.create(FibreViewUiBinder.class);

	interface FibreViewUiBinder extends UiBinder<Widget, FibreView> {}
	
	@UiField
	Modal modal;
	
	@UiField
	FlowPanel chartPanel;

	private IFibrePresenter presenter;
	
	public FibreView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("slider")
	void onSlide(SlideEvent<Double> event) {
		getPresenter().onSliderChanged(event.getValue());
	}

	@Override
	public void showModal(boolean show) {
		modal.show();
	}

	@Override
	public void setChart(Chart chart) {
		chartPanel.add(chart);
	}

	@Override
	public void setPresenter(IFibrePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IFibrePresenter getPresenter() {
		return presenter;
	}
}