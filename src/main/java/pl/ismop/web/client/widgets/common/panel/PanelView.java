package pl.ismop.web.client.widgets.common.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import pl.ismop.web.client.widgets.common.panel.IPanelView.IPanelPresenter;

public class PanelView extends Composite implements IPanelView, ReverseViewInterface<IPanelPresenter> {
    private IPanelPresenter presenter;

    interface PanelViewUiBinder extends UiBinder<Widget, PanelView> {
    }

    private static PanelViewUiBinder uiBinder = GWT.create(PanelViewUiBinder.class);

    @UiField
    Panel panel;

    @UiField
    Heading heading;

    @UiField
    PanelBody panelBody;

    @UiField
    Button upButton;

    @UiField
    Button downButton;

    @UiField
    Button editButton;

    @UiField
    Button closeButton;

    public PanelView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("closeButton")
    void close(ClickEvent event) {
        getPresenter().close();
    }

    @UiHandler("upButton")
    void moveUp(ClickEvent event) {
        setPanelType(PanelType.DEFAULT, ButtonType.DEFAULT);
        getPresenter().moveUp();
    }

    @UiHandler("downButton")
    void moveDown(ClickEvent event) {
        setPanelType(PanelType.DEFAULT, ButtonType.DEFAULT);
        getPresenter().moveDown();
    }

    @UiHandler("editButton")
    void edit(ClickEvent event) {
        getPresenter().edit();
    }

    @UiHandler("panelWrapper")
    void onMouseOver(MouseOverEvent over) {
        setPanelType(PanelType.INFO, ButtonType.INFO);
        getPresenter().mouseOver();
    }

    @UiHandler("panelWrapper")
    void onMouseOut(MouseOutEvent out) {
        setPanelType(PanelType.DEFAULT, ButtonType.DEFAULT);
        getPresenter().mouseOut();
    }

    void setPanelType(PanelType panelType, ButtonType buttonType) {
        panel.setType(panelType);
        upButton.setType(buttonType);
        downButton.setType(buttonType);
        editButton.setType(buttonType);
        closeButton.setType(buttonType);
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
    public void setFirst(boolean first) {
        upButton.setVisible(!first);
    }

    @Override
    public void setLast(boolean last) {
        downButton.setVisible(!last);
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