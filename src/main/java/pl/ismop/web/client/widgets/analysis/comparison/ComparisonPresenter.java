package pl.ismop.web.client.widgets.analysis.comparison;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.threatlevel.ThreatLevel;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.widgets.analysis.chart.ChartPresenter;
import pl.ismop.web.client.widgets.analysis.chart.wizard.ChartWizardPresenter;
import pl.ismop.web.client.widgets.analysis.comparison.IComparisonView.IComparisonPresenter;
import pl.ismop.web.client.widgets.analysis.threatlevels.ThreatLevelsPresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.IWindowManager;
import pl.ismop.web.client.widgets.common.panel.PanelPresenter;
import pl.ismop.web.client.widgets.common.slider.SliderPresenter;

@Presenter(view = ComparisonView.class, multiple = true)
public class ComparisonPresenter extends BasePresenter<IComparisonView, MainEventBus>
        implements IComparisonPresenter, IWindowManager {
    private SliderPresenter sliderPresenter;
    private Experiment selectedExperiment;
    private Set<PanelPresenter> panels = new HashSet<>();
	private List<ThreatLevel> threatLevels;

    public void init() {
        if (sliderPresenter == null) {
            sliderPresenter = eventBus.addHandler(SliderPresenter.class);
            sliderPresenter.setAllowEditDateIntervals(false);
            sliderPresenter.setEventsListener(new SliderPresenter.Events() {
                @Override
                public void onDateChanged(Date selectedDate) {
                    eventBus.dateChanged(selectedDate);
                }
            });
            getView().setSlider(sliderPresenter.getView());
        }

        updateEnabled();
    }


    @Override
    public void addChart() {
        final ChartWizardPresenter wizard = eventBus.addHandler(ChartWizardPresenter.class);
        wizard.show(selectedExperiment, new ChartWizardPresenter.ShowResult() {
            @Override
            public void ok(List<Timeline> selectedTimelines) {
                ChartPresenter content = eventBus.addHandler(ChartPresenter.class);
                content.setWizard(wizard);
                eventBus.addPanel("Chart", content);

                content.setTimelines(selectedTimelines);
            }
        });
    }

    @Override
    public void addHorizontalCS() {
    	eventBus.showHorizontalCrosssectionWizard(selectedExperiment);
    }

    @Override
    public void addVerticalCS() {
    	eventBus.showVerticalCrosssectionWizard(selectedExperiment);
    }

    @Override
    public void addThreadAssesment() {
    	GWT.log("Show thread assesment");
    	ThreatLevelsPresenter content = eventBus.addHandler(ThreatLevelsPresenter.class);
    	content.onThreatLevelsChanged(threatLevels);
    	eventBus.addPanel("Threat levels", content);
    }

    @Override
    public void closePanel(PanelPresenter panel) {
        getView().removePanel(panel.getView());
        panel.destroy();
        eventBus.removeHandler(panel);
        eventBus.clearMinimap();
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
        content.setSelectedDate(sliderPresenter.getSelectedDate());
        content.setSelectedExperiment(selectedExperiment);

        PanelPresenter panel = wrapWithPanel(panelTitle, content);
        getView().addPanel(panel.getView());
        panels.add(panel);
    }

    private PanelPresenter wrapWithPanel(String title, IPanelContent content) {
        PanelPresenter panel = eventBus.addHandler(PanelPresenter.class);
        panel.setWindowManager(this);
        panel.setTitle(title);
        panel.setContent(content);

        return panel;
    }

    @SuppressWarnings("unused")
    public void onExperimentChanged(Experiment selectedExperiment) {
        this.selectedExperiment = selectedExperiment;
        removeAllPanels();
        updateSliderDates();
        updateEnabled();
    }

    private void removeAllPanels() {
        for (PanelPresenter panel : panels) {
            closePanel(panel);
        }
    }

    private void updateSliderDates() {
        if (selectedExperiment != null) {
            Date previousDate = sliderPresenter.getSelectedDate();
            sliderPresenter.setStartDate(selectedExperiment.getStart());
            sliderPresenter.setEndDate(selectedExperiment.getEnd());
            if (!previousDate.equals(sliderPresenter.getSelectedDate())) {
                eventBus.dateChanged(sliderPresenter.getSelectedDate());
            }
        }
    }

    private void updateEnabled() {
        boolean enabled = selectedExperiment != null;
        getView().setActionsEnabled(enabled);
        sliderPresenter.setEnabled(enabled);
    }

    public void onThreatLevelsChanged(List<ThreatLevel> threatLevels) {
    	this.threatLevels = threatLevels;
    }
}