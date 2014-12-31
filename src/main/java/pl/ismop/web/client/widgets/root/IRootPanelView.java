package pl.ismop.web.client.widgets.root;

import com.google.gwt.user.client.ui.IsWidget;

public interface IRootPanelView extends IsWidget {
	interface IRootPresenter {
		void onShowSensors(boolean show);
		void onShowLevees(boolean show);
	}
}