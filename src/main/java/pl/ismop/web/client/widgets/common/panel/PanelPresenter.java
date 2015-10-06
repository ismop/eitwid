package pl.ismop.web.client.widgets.common.panel;

import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.comparison.IPanelContent;

import static pl.ismop.web.client.widgets.common.panel.IPanelView.IPanelPresenter;

@Presenter(view = PanelView.class, multiple = true)
public class PanelPresenter extends BasePresenter<IPanelView, MainEventBus> implements IPanelPresenter {
    private IWindowManager windowManager;
    private IPanelContent<IsWidget, EventBus> content;

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

    @Override
    public void edit() {
        if (content != null) {
            content.edit();
        }
    }

    public void setTitle(String title) {
        getView().setTitle(title);
    }

    public void setContent(IPanelContent<IsWidget, EventBus> content) {
        this.content = content;
        getView().setWidget(content.getView());
    }

    public void destroy() {
        if (content != null) {
            eventBus.removeHandler(content);
        }
    }

    public void setWindowManager(IWindowManager windowManager) {
        this.windowManager = windowManager;
    }
}
