package pl.ismop.web.client.widgets.common.panel;

/**
 * Created by marek on 05.10.15.
 */
public interface IWindowManager {
    void closePanel(PanelPresenter panel);
    void moveUp(PanelPresenter panel);
    void moveDown(PanelPresenter panel);
}
