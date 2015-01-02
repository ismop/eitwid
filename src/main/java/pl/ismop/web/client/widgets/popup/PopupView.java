package pl.ismop.web.client.widgets.popup;

import pl.ismop.web.client.widgets.popup.IPopupView.IPopupPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class PopupView extends Composite implements IPopupView, ReverseViewInterface<IPopupPresenter> {
	private static PopupViewUiBinder uiBinder = GWT.create(PopupViewUiBinder.class);
	interface PopupViewUiBinder extends UiBinder<Widget, PopupView> {}

	@UiField PopupPanel popup;
	@UiField HTMLPanel contents;
	@UiField Label title;
	private IPopupPresenter presenter;
	
	public PopupView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("close")
	void close(ClickEvent event) {
		getPresenter().onClose();
		popup.hide();
	}
	
	@Override
	public void add(IsWidget widget) {
		contents.add(widget);
	}

	@Override
	public void show(boolean show) {
		if(show) {
			popup.setPopupPosition(100, 60);
			popup.show();
		} else {
			popup.hide();
		}
	}

	@Override
	public void clean() {
		contents.clear();
	}
	
	@Override
	public void setTitle(String title) {
		this.title.setText(title);
	}

	@Override
	public void setPresenter(IPopupPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IPopupPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void hide() {
		popup.hide();
	}
}