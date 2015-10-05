package pl.ismop.web.client.widgets.analysis.comparison;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.comparison.IComparisonView.IComparisonPresenter;
import pl.ismop.web.client.widgets.analysis.dumy.DumyPresenter;
import pl.ismop.web.client.widgets.common.panel.IWindowManager;
import pl.ismop.web.client.widgets.common.panel.PanelPresenter;
import pl.ismop.web.client.widgets.common.slider.SliderPresenter;

@Presenter(view = ComparisonView.class, multiple = true)
public class ComparisonPresenter extends BasePresenter<IComparisonView, MainEventBus>
        implements IComparisonPresenter, IWindowManager {
    private SliderPresenter sliderPresenter;

    public void init() {
        if (sliderPresenter == null) {
            sliderPresenter = eventBus.addHandler(SliderPresenter.class);
            sliderPresenter.setAllowEditDateIntervals(false);
            view.setSlider(sliderPresenter.getView());
        }
    }


    @Override
    public void addChart() {
        IPanelContent content = eventBus.addHandler(DumyPresenter.class);
        eventBus.addPanel("New chart", content);
    }

    @Override
    public void addHorizontalCS() {
        IPanelContent content = eventBus.addHandler(DumyPresenter.class);
        eventBus.addPanel("New horizontal cross section", content);
    }

    @Override
    public void addVerticalCS() {
        IPanelContent content = eventBus.addHandler(DumyPresenter.class);
        eventBus.addPanel("New vertical cross section", content);
    }

    @Override
    public void closePanel(PanelPresenter panel) {
        getView().removePanel(panel.getView());
        panel.destroy();
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

    @SuppressWarnings("unused")
    public void onAddPanel(String panelTitle, IPanelContent content) {
        PanelPresenter panel = wrapWithPanel(panelTitle, content);
        getView().addPanel(panel.getView());
    }

    private PanelPresenter wrapWithPanel(String title, IPanelContent content) {
        PanelPresenter panel = eventBus.addHandler(PanelPresenter.class);
        panel.setWindowManager(this);
        panel.setTitle(title);
        panel.setContent(content);

        return panel;
    }
}