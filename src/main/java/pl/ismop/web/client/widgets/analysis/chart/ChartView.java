package pl.ismop.web.client.widgets.analysis.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ChartView extends Composite implements IChartView {
    private static ChartViewUiBinder uiBinder = GWT.create(ChartViewUiBinder.class);

    interface ChartViewUiBinder extends UiBinder<Widget, ChartView> {}

    public ChartView() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
