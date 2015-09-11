package pl.ismop.web.client.widgets.monitoring.mapnavigator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class LeveeNavigatorView extends Composite implements ILeveeNavigatorView {
	private static LeveeNavigatorViewUiBinder uiBinder = GWT.create(LeveeNavigatorViewUiBinder.class);
	
	interface LeveeNavigatorViewUiBinder extends UiBinder<Widget, LeveeNavigatorView> {}

	@UiField
	FlowPanel loadingProgress;
	
	@UiField
	FlowPanel mapContainer;
	
	public LeveeNavigatorView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setMap(IsWidget view) {
		mapContainer.add(view);
	}

	@Override
	public void showMap(boolean show) {
		mapContainer.setVisible(show);
	}

	@Override
	public void showProgress(boolean show) {
		loadingProgress.setVisible(show);
	}
}