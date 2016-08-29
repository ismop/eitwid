package pl.ismop.web.client.widgets.realtime.side;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.realtime.side.IRealTimeSidePanelView.IRealTimeSidePanelPresenter;

public class RealTimeSidePanelView extends Composite implements IRealTimeSidePanelView , ReverseViewInterface<IRealTimeSidePanelPresenter> {
	private static RealTimeSidePanelViewUiBinder uiBinder = GWT.create(RealTimeSidePanelViewUiBinder.class);

	interface RealTimeSidePanelViewUiBinder extends UiBinder<Widget, RealTimeSidePanelView> {}

	private IRealTimeSidePanelPresenter presenter;

	@UiField
	FlowPanel mapContainer;

	@UiField
	FlowPanel progressContainer;

	public RealTimeSidePanelView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setPresenter(IRealTimeSidePanelPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IRealTimeSidePanelPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setMapView(IsWidget view) {
		mapContainer.add(view);
	}

	@Override
	public void setProegressView(IsWidget view) {
		progressContainer.add(view);
	}
}