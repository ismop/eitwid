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

    @UiHandler("showDevice1")
    void onShowDevice1(ClickEvent event) {
        getPresenter().showDevice1();
    }

    @UiHandler("showDevice2")
    void onShowDevice2(ClickEvent event) {
        getPresenter().showDevice2();
    }

    @UiHandler("selectDevice1")
    void onSelectDevice1(ClickEvent event) {
        getPresenter().selectDevice1();
    }

    @UiHandler("unselectDevice1")
    void onUnselectDevice1(ClickEvent event) {
        getPresenter().unselectDevice1();
    }

    @UiHandler("selectDevice2")
    void onSelectDevice2(ClickEvent event) {
        getPresenter().selectDevice2();
    }

    @UiHandler("unselectDevice2")
    void onUnselectDevice2(ClickEvent event) {
        getPresenter().unselectDevice2();
    }

    @UiHandler("showSection1")
    void onShowSection1(ClickEvent event) {
        getPresenter().showSection1();
    }

    @UiHandler("showSection2")
    void onShowSections2(ClickEvent event) {
        getPresenter().showSection2();
    }

    @UiHandler("showProfile1")
    void onShowProfile1(ClickEvent event) {
        getPresenter().showProfile1();
    }

    @UiHandler("showProfile2")
    void onShowProfile2(ClickEvent event) {
        getPresenter().showProfile2();
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