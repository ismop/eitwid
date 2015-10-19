package pl.ismop.web.client.widgets.analysis.sidepanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import org.moxieapps.gwt.highcharts.client.*;
import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.IsmopProperties;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.sidepanel.IAnalysisSidePanelView.IAnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.delegator.MeasurementsCallback;
import pl.ismop.web.client.widgets.delegator.ParametersCallback;

import java.util.*;

@Presenter(view = AnalysisSidePanelView.class, multiple = true)
public class AnalysisSidePanelPresenter extends BasePresenter<IAnalysisSidePanelView, MainEventBus> implements IAnalysisSidePanelPresenter {
    private final DapController dapController;
    private final IsmopProperties properties;
    private final IsmopConverter converter;
    private PlotLine currentTimePlotLine;

    private MapPresenter miniMap;
    private Chart waterWave;

    private Experiment selectedExperiment;
    private AnalysisSidePanelMessages messages;
    private Device shownDevice;
    private Set<Device> selectedDevices = new HashSet<>();
    private Section shownSection;
    private Profile shownProfile;

    @Inject
    public AnalysisSidePanelPresenter(DapController dapController, IsmopProperties properties, IsmopConverter converter) {
        this.dapController = dapController;
        this.properties = properties;
        this.converter = converter;
    }

    public void init() {
        this.messages = getView().getMessages();
        initExperiments();
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
        }
    }

    @Override
    public void selectExperiment(Experiment selectedExperiment) {
        if (this.selectedExperiment != selectedExperiment) {
            this.selectedExperiment = selectedExperiment;
            initWaterWave();
            initMinimap();
            loadExperimentWaveShape();
            loadExperimentLevee();
            eventBus.experimentChanged(selectedExperiment);
        }
    }

    private void loadExperimentWaveShape() {
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

    private void loadExperimentLevee() {
        miniMap.reset(false);
        dapController.getSections(selectedExperiment.getLeveeId() + "", new DapController.SectionsCallback() {
            @Override
            public void processSections(List<Section> sections) {
                selectedExperiment.setSections(sections);
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

    private void showExperimentWaveShape(Map<Parameter, List<Measurement>> series) {
        waterWave.removeAllSeries();
        Parameter parameter = null;
        for (Map.Entry<Parameter, List<Measurement>> entry : series.entrySet()) {
            parameter = entry.getKey();
            Series s = waterWave.createSeries().
                    setType(Series.Type.SPLINE).
                    setName(parameter.getParameterName());

            long diff = 0;
            if(entry.getValue().size() > 0) {
                diff = converter.parse(entry.getValue().get(0).getTimestamp()).getTime() -
                        selectedExperiment.getStart().getTime();
            }

            for (Measurement measurement : entry.getValue()) {
                long time = converter.parse(measurement.getTimestamp()).getTime() - diff;
                if (time > selectedExperiment.getEnd().getTime()) {
                    GWT.log("Warning experiment water wave is longer then experiment");
                    break;
                }

                s.addPoint(time, measurement.getValue());
            }
            waterWave.addSeries(s);
        }

        if(parameter != null) {
            waterWave.getYAxis().setAxisTitleText(parameter.getMeasurementTypeName() + " [" + parameter.getMeasurementTypeUnit() + "]");
        }
    }

    @SuppressWarnings("unused")
    public void onDateChanged(Date selectedDate) {
        if (waterWave != null) {
            if (currentTimePlotLine != null) {
                waterWave.getXAxis().removePlotLine(currentTimePlotLine);
            }
            currentTimePlotLine = waterWave.getXAxis().createPlotLine().
                    setWidth(2).setColor(properties.selectionColor()).setValue(selectedDate.getTime());
            waterWave.getXAxis().addPlotLines(currentTimePlotLine);
        }
    }

    @SuppressWarnings("unused")
    public void onSelectDevice(Device device) {
        miniMap.selectDevice(device, true);
        selectedDevices.add(device);
    }

    @SuppressWarnings("unused")
    public void onUnselectDevice(Device device) {
        if (shownDevice == device) {
            miniMap.selectDevice(device, false);
        } else {
            miniMap.removeDevice(device);
        }
    }

    @SuppressWarnings("unused")
    public void onShowDevice(Device device) {
        if (shownDevice != null) {
            if (selectedDevices.contains(shownDevice)) {
                miniMap.selectDevice(shownDevice, true);
            } else {
                miniMap.removeDevice(shownDevice);
            }
        }
        shownDevice = device;
        miniMap.selectDevice(device, false);

    }

    @SuppressWarnings("unused")
    public void onShowSection(Section section) {
        if (shownSection != null) {
            miniMap.highlightSection(shownSection, false);
        }
        shownSection = section;
        miniMap.highlightSection(section, true);
    }

    @SuppressWarnings("unused")
    public void onShowProfile(Profile profile) {
        GWT.log("Show profile" + profile.getId());
        if (shownProfile != null) {
            miniMap.removeProfile(shownProfile);
        }
        shownProfile = profile;
        miniMap.addProfile(profile);
    }

    @SuppressWarnings("unused")
    public void onClearMinimap() {
        if(shownProfile != null) {
            miniMap.removeProfile(shownProfile);
            shownProfile = null;
        }

        if (shownSection != null) {
            miniMap.highlightSection(shownSection, false);
            shownSection = null;
        }

        if (shownDevice != null) {
            miniMap.removeDevice(shownDevice);
            shownDevice = null;
        }

        for (Device selectedDevice : selectedDevices) {
            miniMap.removeDevice(selectedDevice);
        }
        selectedDevices.clear();
    }
}