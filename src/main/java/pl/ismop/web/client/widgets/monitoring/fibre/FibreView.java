package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.user.client.ui.IsWidget;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Label;
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

import pl.ismop.web.client.widgets.monitoring.fibre.IFibreView.IFibrePresenter;
import pl.ismop.web.client.widgets.slider.SliderView;

public class FibreView extends Composite implements IFibreView, ReverseViewInterface<IFibrePresenter> {
	private static FibreViewUiBinder uiBinder = GWT.create(FibreViewUiBinder.class);

	interface FibreViewUiBinder extends UiBinder<Widget, FibreView> {}
	
	private IFibrePresenter presenter;

	@UiField
	FibreMessages messages;

	@UiField
	Modal modal;
	
	@UiField
	FlowPanel leftPanel;

	@UiField
	FlowPanel rightPanel;

	public FibreView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("modal")
	void onPopupReady(ModalShownEvent event) {
		getPresenter().onModalReady();
	}
	
	@Override
	public void showModal(boolean show) {
		modal.show();
	}

	@Override
	public void addElementToLeftPanel(IsWidget widget) {
		leftPanel.add(widget);
	}

	@Override
	public void addElementToRightPanel(IsWidget widget) {
		rightPanel.add(widget);
	}

	@Override
	public int getLeftPanelWidth() {
		return getOffsetWidth();
	}

	@Override
	public void setPresenter(IFibrePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IFibrePresenter getPresenter() {
		return presenter;
	}

	@Override
	public FibreMessages getMessages() {
		return messages;
	}
}