package pl.ismop.web.client.widgets.common.panel;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;

import static pl.ismop.web.client.widgets.common.panel.IPanelView.IPanelPresenter;

@Presenter(view = PanelView.class, multiple = true)
public class PanelPresenter extends BasePresenter<IPanelView, MainEventBus> implements IPanelPresenter {

    @Override
    public void close() {
        Window.alert("close");
    }

    @Override
    public void moveUp() {
        Window.alert("move up");
    }

    @Override
    public void moveDown() {
        Window.alert("move down");
    }

    public void setTitle(String title) {
        getView().setTitle(title);
    }

    public void setContent(IsWidget widget) {
        getView().setWidget(widget);
    }
}
