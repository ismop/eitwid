package pl.ismop.web.client.widgets.analysis.sidepanel;

import java.util.List;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.sidepanel.IAnalysisSidePanelView.IAnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;

@Presenter(view = AnalysisSidePanelView.class, multiple = true)
public class AnalysisSidePanelPresenter extends BasePresenter<IAnalysisSidePanelView, MainEventBus> implements IAnalysisSidePanelPresenter {
    private final DapController dapController;

    private MapPresenter miniMap;
    private Chart waterWave;

    private List<Experiment> experiments;
    private Experiment selectedExperiment;

    @Inject
    public AnalysisSidePanelPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    public void init() {
        initExperiments();
        initWaterWave();
        initMinimap();
	}

    private void initExperiments() {
        dapController.getExperiments(new DapController.ExperimentsCallback() {
            @Override
            public void processExperiments(List<Experiment> loadedExperiments) {
                getView().setExperiments(loadedExperiments);
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });
    }

    private void initWaterWave() {
        if (waterWave == null) {
            waterWave = new Chart().
                    setChartTitle(new ChartTitle().setText("Water wave"));

            waterWave.getXAxis().
                    setAxisTitle(new AxisTitle().setText("Time"));

            Series wave = waterWave.createSeries().
                    setName("Water wave").
                    setType(Series.Type.LINE);
            wave.addPoint(0, 0).addPoint(1, 1).addPoint(5, 1).addPoint(6, 0);
            waterWave.addSeries(wave);
            view.setWaterWavePanel(waterWave);
        }
    }

    private void initMinimap() {
        if (miniMap == null) {
            miniMap = eventBus.addHandler(MapPresenter.class);

            view.setMinimap(miniMap.getView());

            dapController.getSections("1", new DapController.SectionsCallback() {
                @Override
                public void processSections(List<Section> sections) {
                    for (Section section : sections) {
                        miniMap.addSection(section);
                    }
                }

                @Override
                public void onError(ErrorDetails errorDetails) {
                    eventBus.showError(errorDetails);
                }
            });
        }
    }

    @Override
    public void selectExperiment(Experiment selectedExperiment) {
        this.selectedExperiment = selectedExperiment;
        eventBus.experimentChanged(selectedExperiment);
    }
}