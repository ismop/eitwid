package pl.ismop.web.client.widgets.common.panel;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;

import static pl.ismop.web.client.widgets.common.panel.IPanelView.IPanelPresenter;

@Presenter(view = PanelView.class, multiple = true)
public class PanelPresenter extends BasePresenter<IPanelView, MainEventBus> implements IPanelPresenter {
    IWindowManager windowManager;

    @Override
    public void close() {
        windowManager.closePanel(this);
    }

    @Override
    public void moveUp() {
        windowManager.moveUp(this);
    }

    @Override
    public void moveDown() {
        windowManager.moveDown(this);
    }

    public void setTitle(String title) {
        getView().setTitle(title);
    }

    public void setContent(IsWidget widget) {
        getView().setWidget(widget);
    }

    public void setWindowManager(IWindowManager windowManager) {
        this.windowManager = windowManager;
    }
}
