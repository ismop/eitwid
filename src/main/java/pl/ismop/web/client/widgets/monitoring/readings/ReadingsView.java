package pl.ismop.web.client.widgets.monitoring.readings;

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

import pl.ismop.web.client.widgets.monitoring.readings.IReadingsView.IReadingsPresenter;

public class ReadingsView extends Composite implements IReadingsView, ReverseViewInterface<IReadingsPresenter> {
	private static ReadingsViewUiBinder uiBinder = GWT.create(ReadingsViewUiBinder.class);

	interface ReadingsViewUiBinder extends UiBinder<Widget, ReadingsView> {}
	
	private IReadingsPresenter presenter;

	@UiField
	Modal modal;
	
	@UiField
	FlowPanel mapContainer, miscContainer, chartContainer;
	
	public ReadingsView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("modal")
	void modalShown(ModalShownEvent event) {
		getPresenter().onModalShown();
	}

	@Override
	public void showModal(boolean show) {
		if(show) {
			modal.show();
		} else {
			modal.hide();
		}
	}

	@Override
	public void setPresenter(IReadingsPresenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public IReadingsPresenter getPresenter() {
		return presenter;
	}
	
	@Override
	public void setMap(IsWidget map) {
		mapContainer.add(map);
	}

	@Override
	public void setChart(IsWidget chart) {
		chartContainer.add(chart);
	}
}