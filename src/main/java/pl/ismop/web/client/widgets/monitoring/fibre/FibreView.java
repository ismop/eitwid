package pl.ismop.web.client.widgets.monitoring.fibre;

import com.google.gwt.user.client.ui.IsWidget;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.monitoring.fibre.IFibreView.IFibrePresenter;

public class FibreView extends Composite implements IFibreView, ReverseViewInterface<IFibrePresenter> {
	private static FibreViewUiBinder uiBinder = GWT.create(FibreViewUiBinder.class);

	interface FibreViewUiBinder extends UiBinder<Widget, FibreView> {}
	
	private IFibrePresenter presenter;

	@UiField
	FibreMessages messages;

	@UiField
	Modal modal;
	
	@UiField
	FlowPanel sliderPanel;

	@UiField
	FlowPanel fibreDevicesPanel;

	@UiField
	FlowPanel selectedDevicesPanel;

	@UiField
	FlowPanel mapPanel;

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
	public void setMap(IsWidget widget) {
		mapPanel.add(widget);
	}

	@Override
	public void setSlider(IsWidget widget) {
		sliderPanel.add(widget);
	}

	@Override
	public void setFibreDevices(IsWidget widget) {
		fibreDevicesPanel.add(widget);
	}

	@Override
	public void setSelectedDevices(IsWidget widget) {
		selectedDevicesPanel.add(widget);
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