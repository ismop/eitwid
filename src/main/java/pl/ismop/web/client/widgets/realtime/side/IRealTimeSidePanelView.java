package pl.ismop.web.client.widgets.realtime.side;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRealTimeSidePanelView extends IsWidget {
	interface IRealTimeSidePanelPresenter {
	}

	void setMapView(IsWidget view);

	void setProegressView(IsWidget view);
}