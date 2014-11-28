package pl.ismop.web.client.widgets.profile;

import com.google.gwt.user.client.ui.IsWidget;

public interface IProfileView extends IsWidget {
	interface IProfilePresenter {
		
	}

	void setHeader(String name);
	void setModeStyle(String panelStyle);
	void setThreatLevel(String threatLevel);
}