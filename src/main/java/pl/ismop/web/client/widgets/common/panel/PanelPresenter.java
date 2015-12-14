package pl.ismop.web.client.widgets.common.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;

import static pl.ismop.web.client.widgets.common.panel.IPanelView.IPanelPresenter;

@Presenter(view = PanelView.class, multiple = true)
public class PanelPresenter extends BasePresenter<IPanelView, MainEventBus> implements IPanelPresenter {
    private IWindowManager windowManager;
    private SelectionManager selectionManager;
    private IPanelContent<IsWidget, EventBus> content;

    @Override
    public void bind() {
        selectionManager = new SelectionManager(eventBus);
    }

    @Override
    public void close() {
        windowManager.closePanel(this);
    }

    @Override
    public void moveUp() {
        windowManager.moveUp(this);
        selectionManager.deactivate();
    }

    @Override
    public void moveDown() {
        windowManager.moveDown(this);
        selectionManager.deactivate();
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
        content.setSelectionManager(selectionManager);
        getView().setWidget(content.getView());
    }

    public void destroy() {
        if (content != null) {
            eventBus.removeHandler(content);
        }
    }

    @Override
    public void mouseOver() {
        selectionManager.activate();
    }

    @Override
    public void mouseOut() {
        selectionManager.deactivate();
    }

    public void setWindowManager(IWindowManager windowManager) {
        this.windowManager = windowManager;
    }
}
