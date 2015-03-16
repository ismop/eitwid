package pl.ismop.web.client.widgets.levees;

import com.google.gwt.user.client.ui.IsWidget;

public interface ILeveesView extends IsWidget {
	interface ILeveesPresenter {
		
	}

	String popupTitle();
	void showNoLeveesMessage();
	void addLeveeWidget(IsWidget view);
}