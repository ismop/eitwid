package pl.ismop.web.client.widgets.analysis.sidepanel;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import org.moxieapps.gwt.highcharts.client.*;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.sidepanel.IAnalysisSidePanelView.IAnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.delegator.MeasurementsCallback;
import pl.ismop.web.client.widgets.delegator.ParametersCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Presenter(view = AnalysisSidePanelView.class, multiple = true)
public class AnalysisSidePanelPresenter extends BasePresenter<IAnalysisSidePanelView, MainEventBus> implements IAnalysisSidePanelPresenter {
    private final DapController dapController;

    private MapPresenter miniMap;
    private Chart waterWave;

    private Experiment selectedExperiment;
    private AnalysisSidePanelMessages messages;

    @Inject
    public AnalysisSidePanelPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    public void init() {
        this.messages = getView().getMessages();
        initExperiments();
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
                    setChartTitle(new ChartTitle().setText(messages.waterWaveChartTitle()));

            waterWave.setHeight(view.getWaterWavePanelHeight());
            waterWave.setOption("/chart/zoomType", "x");

            waterWave.getXAxis().
                    setType(Axis.Type.DATE_TIME).
                    setAxisTitle(new AxisTitle().setText(messages.time())).
                    setDateTimeLabelFormats(new DateTimeLabelFormats().
                            setMonth("%e. %b").
                            setYear("%b"));

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
        loadExperimentWaveShape();
    }

    private void loadExperimentWaveShape() {
        initWaterWave();

        if (selectedExperiment != null) {
            waterWave.showLoading(messages.loadingWaterWave());
            dapController.getExperimentTimelines(selectedExperiment.getId(), new DapController.TimelinesCallback() {
                @Override
                public void processTimelines(final List<Timeline> timelines) {
                    final Map<String, String> parameterIdToTimelineId = new HashMap<>();
                    for (Timeline timeline : timelines) {
                        parameterIdToTimelineId.put(timeline.getParameterId(), timeline.getId());
                    }
                    dapController.getParametersById(parameterIdToTimelineId.keySet(), new ParametersCallback(this) {
                        @Override
                        public void processParameters(final List<Parameter> parameters) {
                            final Map<String, Parameter> timelineIdToParameter = new HashMap<>();
                            for (Parameter parameter : parameters) {
                                timelineIdToParameter.put(parameterIdToTimelineId.get(parameter.getId()), parameter);
                            }
                            dapController.getAllMeasurements(parameterIdToTimelineId.values(), new MeasurementsCallback(this) {
                                @Override
                                public void processMeasurements(List<Measurement> measurements) {
                                    Map<Parameter, List<Measurement>> series = new HashMap<>();
                                    for (Measurement measurement : measurements) {
                                        Parameter parameter = timelineIdToParameter.get(measurement.getTimelineId());
                                        List<Measurement> m = series.get(parameter);
                                        if (m == null) {
                                            m = new ArrayList<>();
                                            series.put(parameter, m);
                                        }
                                        m.add(measurement);
                                    }

                                    showExperimentWaveShape(series);
                                    waterWave.hideLoading();
                                }
                            });
                        }
                    });
                }

                @Override
                public void onError(ErrorDetails errorDetails) {
                    eventBus.showError(errorDetails);
                }
            });
        }
    }

    private void showExperimentWaveShape(Map<Parameter, List<Measurement>> series) {
        waterWave.removeAllSeries();
        DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);
        Parameter parameter = null;
        for (Map.Entry<Parameter, List<Measurement>> entry : series.entrySet()) {
            parameter = entry.getKey();
            Series s = waterWave.createSeries().
                    setType(Series.Type.SPLINE).
                    setName(parameter.getParameterName());
            for (Measurement measurement : entry.getValue()) {
                s.addPoint(format.parse(measurement.getTimestamp()).getTime(), measurement.getValue());
            }
            waterWave.addSeries(s);
        }

        if(parameter != null) {
            waterWave.getYAxis().setAxisTitleText(parameter.getMeasurementTypeName() + " [" + parameter.getMeasurementTypeUnit() + "]");
        }
    }
}