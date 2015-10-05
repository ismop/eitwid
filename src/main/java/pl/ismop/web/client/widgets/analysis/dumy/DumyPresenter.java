package pl.ismop.web.client.widgets.analysis.dumy;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.comparison.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.IPanelView;

import java.util.Date;

@Presenter(view = DumyView.class, multiple = true)
public class DumyPresenter extends BasePresenter<IDumyView, MainEventBus> implements IPanelContent<IDumyView, MainEventBus> {
    Date date;
    String experiment;

    @Override
    public void setSelectedExperiment() {
        experiment = "This will be experiment object";
        updateTitle();
    }

    @Override
    public void setSelectedDate(Date date) {
        this.date = date;
        updateTitle();
    }

    private void updateTitle() {
        String title = "";
        if (experiment != null) {
            title = title + "Selected experiment " + experiment + " ";
        }

        if (date != null) {
            title = title + "Selected time: " + date;
        }

        getView().setTitle(title);
    }
}
