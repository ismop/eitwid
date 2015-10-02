package pl.ismop.web.client.widgets.analysis.sidepanel;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import org.gwtbootstrap3.client.ui.Button;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.sidepanel.IAnalysisSidePanel.IAnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;

@Presenter(view = AnalysisSidePanel.class, multiple = true)
public class AnalysisSidePanelPresenter extends BasePresenter<IAnalysisSidePanel, MainEventBus> implements IAnalysisSidePanelPresenter {
    private final DapController dapController;

    private MapPresenter miniMap;
    private Chart waterWave;

    private Button addChart;
    private Button addHorizontalCrossSection;
    private Button addVerticalCrossSection;

    @Inject
    public AnalysisSidePanelPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    public void init() {
        initWaterWave();
        initMinimap();
        initActionButtons();
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

            GWT.log(">>>>" + view.getWaterWavePanelHeight() + ">>> " + view.getWaterWavePanelWidth());

//            waterWave.setWidth(view.getWaterWavePanelWidth());
//            waterWave.setHeight(view.getWaterWavePanelHeight());
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

    private void initActionButtons() {
        if (addChart == null) {
            addChart = new Button("Add new chart...", new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    Window.alert("TODO: Add chart");
                }
            });

            addHorizontalCrossSection = new Button("Add horizontal cross section...", new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    eventBus.showHorizontalCrosssectionWizard();
                }
            });

            addVerticalCrossSection = new Button("Add vertical cross section...", new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    Window.alert("TODO: Add vertical cross section");
                }
            });

            view.addAction(addChart);
            view.addAction(addHorizontalCrossSection);
            view.addAction(addVerticalCrossSection);
        }
    }
}