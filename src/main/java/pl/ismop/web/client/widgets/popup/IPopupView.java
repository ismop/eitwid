package pl.ismop.web.client.widgets.popup;

import com.google.gwt.user.client.ui.IsWidget;

public interface IPopupView {
	interface IPopupPresenter {
		void onClose();
	}

	void show(boolean show);
	void clean();
	void add(IsWidget widget);
	void setTitle(String title);
	void hide();
}