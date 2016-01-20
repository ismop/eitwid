package pl.ismop.web.client.widgets.analysis.dummy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.ui.Label;
import pl.ismop.web.client.widgets.analysis.dummy.IDummyView.IDumyPresenter;

/**
 * Created by marek on 05.10.15.
 */
public class DummyView extends Composite implements IDummyView, ReverseViewInterface<IDumyPresenter> {

    private IDumyPresenter presenter;

    interface DumyViewUiBinder extends UiBinder<Widget, DummyView> {
    }

    private static DumyViewUiBinder uiBinder = GWT.create(DumyViewUiBinder.class);

    @UiField
    Label label;

    public DummyView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setTitle(String title) {
        label.setText(title);
    }

    @UiHandler("addDevice")
    void addDevice(ClickEvent event) {
        getPresenter().addDevice();
    }

    @UiHandler("rmDevice")
    void rmDevice(ClickEvent event) {
        getPresenter().rmDevice();
    }

    @UiHandler("addSection")
    void addSection(ClickEvent event) {
        getPresenter().addSection();
    }

    @UiHandler("rmSection")
    void rmSection(ClickEvent event) {
        getPresenter().rmSection();
    }

    @UiHandler("addProfile")
    void addProfile(ClickEvent event) {
        getPresenter().addProfile();
    }

    @UiHandler("rmProfile")
    void rmProfile(ClickEvent event) {
        getPresenter().rmProfile();
    }

    @UiHandler("selectDevice")
    void selectDevice(ClickEvent event) {
        getPresenter().selectDevice();
    }

    @UiHandler("unselectDevice")
    void unselectDevice(ClickEvent event) {
        getPresenter().unselectDevice();
    }

    @UiHandler("selectSection")
    void selectSection(ClickEvent event) {
        getPresenter().selectSection();
    }

    @UiHandler("unselectSection")
    void unselectSection(ClickEvent event) {
        getPresenter().unselectSection();
    }

    @UiHandler("selectProfile")
    void selectProfile(ClickEvent event) {
        getPresenter().selectProfile();
    }

    @UiHandler("unselectProfile")
    void unselectProfile(ClickEvent event) {
        getPresenter().unselectProfile();
    }

    @UiHandler("highlightDevice")
    void highlightDevice(ClickEvent event) {
        getPresenter().highlightDevice();
    }

    @UiHandler("unhighlightDevice")
    void unhighlightDevice(ClickEvent event) {
        getPresenter().unhighlightDevice();
    }

    @UiHandler("highlightSection")
    void highlightSection(ClickEvent event) {
        getPresenter().highlightSection();
    }

    @UiHandler("unhighlightSection")
    void unhighlightSection(ClickEvent event) {
        getPresenter().unhighlightSection();
    }

    @UiHandler("highlightProfile")
    void highlightProfile(ClickEvent event) {
        getPresenter().highlightProfile();
    }

    @UiHandler("unhighlightProfile")
    void unhighlightProfile(ClickEvent event) {
        getPresenter().highlightProfile();
    }

    @Override
    public void setPresenter(IDumyPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public IDumyPresenter getPresenter() {
        return presenter;
    }
}