package pl.ismop.web.client.widgets.common.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import pl.ismop.web.client.widgets.common.panel.IPanelView.IPanelPresenter;

public class PanelView extends Composite implements IPanelView, ReverseViewInterface<IPanelPresenter> {
    private IPanelPresenter presenter;

    interface PanelViewUiBinder extends UiBinder<Widget, PanelView> {
    }

    private static PanelViewUiBinder uiBinder = GWT.create(PanelViewUiBinder.class);

    @UiField
    Heading heading;

    @UiField
    PanelBody panelBody;

    public PanelView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("closeButton")
    void close(ClickEvent event) {
        getPresenter().close();
    }

    @UiHandler("upButton")
    void moveUp(ClickEvent event) {
        getPresenter().moveUp();
    }

    @UiHandler("downButton")
    void moveDown(ClickEvent event) {
        getPresenter().moveDown();
    }

    @Override
    public void setTitle(String title) {
        heading.setText(title);
    }

    @Override
    public void setWidget(IsWidget widget) {
        panelBody.clear();
        panelBody.add(widget);
    }

    @Override
    public void setPresenter(IPanelPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public IPanelPresenter getPresenter() {
        return presenter;
    }
}