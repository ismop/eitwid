package pl.ismop.web.client.widgets.analysis.dumy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;
import org.gwtbootstrap3.client.ui.Label;
import pl.ismop.web.client.widgets.analysis.comparison.IComparisonView;

/**
 * Created by marek on 05.10.15.
 */
public class DumyView extends Composite implements IDumyView {
    interface DumyViewUiBinder extends UiBinder<Widget, DumyView> {
    }

    private static DumyViewUiBinder uiBinder = GWT.create(DumyViewUiBinder.class);

    @UiField
    Label label;

    public DumyView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setTitle(String title) {
        label.setText(title);
    }
}