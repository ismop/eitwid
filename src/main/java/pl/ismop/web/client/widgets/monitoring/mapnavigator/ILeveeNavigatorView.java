package pl.ismop.web.client.widgets.monitoring.mapnavigator;

import com.google.gwt.user.client.ui.IsWidget;

public interface ILeveeNavigatorView extends IsWidget {
	interface ILeveeNavigatorPresenter {
		
	}

	void setMap(IsWidget view);

	void showMap(boolean show);

	void showProgress(boolean show);

	String getZoomOutLabel();

	void setProfile(IsWidget view);

	void showProfile(boolean show);

	int getProfileContainerWidth();

	int getProfileContainerHeight();
}