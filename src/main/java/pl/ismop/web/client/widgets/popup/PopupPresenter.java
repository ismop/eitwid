package pl.ismop.web.client.widgets.popup;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.popup.IPopupView.IPopupPresenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = PopupView.class)
public class PopupPresenter extends BasePresenter<IPopupView, MainEventBus> implements IPopupPresenter {
	public void onSetTitleAndShow(String title, IsWidget widget, boolean resizable) {
		view.clean();
		view.setTitle(title);
		view.add(widget);
		
		if(resizable) {
			view.showResizableHandler(true);
		} else {
			view.showResizableHandler(false);
		}
		
		view.show(true);
	}

	@Override
	public void onClose() {
		eventBus.popupClosed();
		view.hide();
	}
}