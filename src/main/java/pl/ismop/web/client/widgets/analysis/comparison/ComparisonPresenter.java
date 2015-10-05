package pl.ismop.web.client.widgets.analysis.comparison;

import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import org.gwtbootstrap3.client.ui.Label;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.comparison.IComparisonView.IComparisonPresenter;
import pl.ismop.web.client.widgets.common.panel.IWindowManager;
import pl.ismop.web.client.widgets.common.panel.PanelPresenter;
import pl.ismop.web.client.widgets.slider.SliderPresenter;

@Presenter(view = ComparisonView.class, multiple = true)
public class ComparisonPresenter extends BasePresenter<IComparisonView, MainEventBus>
        implements IComparisonPresenter, IWindowManager {
    private SliderPresenter sliderPresenter;

    public void init() {
        if (sliderPresenter == null) {
            sliderPresenter = eventBus.addHandler(SliderPresenter.class);

            view.setSlider(sliderPresenter.getView());
        }
    }


    @Override
    public void addChart() {
        addPanel("New chart" , new Label("new chart window content"));
    }

    @Override
    public void addHorizontalCS() {
        addPanel("New horizontal cross section", new Label("new horizontal cross section"));
    }

    @Override
    public void addVerticalCS() {
        addPanel("New vertical cross section", new Label("new vertical cross section"));
    }

    private void addPanel(String title, IsWidget content) {
        PanelPresenter panel = wrapWithPanel(title , content);
        getView().addPanel(panel.getView());
    }

    private PanelPresenter wrapWithPanel(String title, IsWidget content) {
        PanelPresenter panel = eventBus.addHandler(PanelPresenter.class);
        panel.setWindowManager(this);
        panel.setTitle(title);
        panel.setContent(content);

        return panel;
    }

    @Override
    public void closePanel(PanelPresenter panel) {
        getView().removePanel(panel.getView());
        eventBus.removeHandler(panel);
    }

    @Override
    public void moveUp(PanelPresenter panel) {
        getView().movePanelUp(panel.getView());
    }

    @Override
    public void moveDown(PanelPresenter panel) {
        getView().movePanelDown(panel.getView());
    }
}