package pl.ismop.web.client.widgets.analysis.chart;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.IsmopProperties;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.chart.wizard.ChartWizardPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

import java.util.*;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus>
        implements IPanelContent<IChartView, MainEventBus> {
    private final DapController dapController;
    
    private final IsmopProperties properties;
    
    private Experiment selectedExperiment;
    
    private ISelectionManager selectionManager;
    
    private ChartWizardPresenter wizard;

    private pl.ismop.web.client.widgets.common.chart.ChartPresenter chartPresenter;

    private Map<String, Device> idToDevice = new HashMap<>();

    @Inject
    public ChartPresenter(DapController dapController, IsmopProperties properties) {
        this.dapController = dapController;
        this.properties = properties;
    }

    @Override
    public void setSelectedExperiment(Experiment experiment) {
        selectedExperiment = experiment;
        getChart().setInterval(experiment.getStart(), experiment.getEnd());
    }

    @Override
    public void setSelectedDate(Date date) {
        onDateChanged(date);
    }

    @Override
    public void edit() {
        wizard.show(selectedExperiment, new ChartWizardPresenter.ShowResult() {
            @Override
            public void ok(List<Timeline> selectedTimelines) {
                setTimelines(selectedTimelines);
            }
        });
    }

    @Override
    public void setSelectionManager(ISelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @Override
    public void destroy() {
        eventBus.removeHandler(wizard);
    }

    interface ChartPointsCallback {
        void processChartPoints(List<ChartSeries> series);
    }

    class ChartPointMeasurementsCallback implements DapController.MeasurementsCallback {
        private final Map<String, Timeline> idToTimeline;
        private final ChartPointsCallback callback;

        public ChartPointMeasurementsCallback(Map<String, Timeline> idToTimeline, ChartPointsCallback callback) {
            this.idToTimeline = idToTimeline;
            this.callback = callback;
        }

        @Override
        public void processMeasurements(List<Measurement> measurements) {
            callback.processChartPoints(map(measurements));
        }

        private List<ChartSeries> map(List<Measurement> measurements) {
            List<ChartSeries> series = new ArrayList<>();
            for (Map.Entry<Timeline, List<Measurement>> timelineListEntry : group(measurements)) {
                ChartSeries s = createSeries(timelineListEntry.getKey());
                s.setValues(getValues(timelineListEntry.getValue(), getDiff(timelineListEntry)));

                series.add(s);
            }

            return series;
        }

        private Set<Map.Entry<Timeline, List<Measurement>>> group(List<Measurement> measurements) {
            Map<Timeline, List<Measurement>> timelineToMeasurements = new HashMap<>();
            for (Measurement measurement : measurements) {
                Timeline timeline = idToTimeline.get(measurement.getTimelineId());
                List<Measurement> timelineMeasurements = timelineToMeasurements.get(timeline);
                if (timelineMeasurements == null) {
                    timelineMeasurements = new ArrayList<>();
                    timelineToMeasurements.put(timeline, timelineMeasurements);
                }

                timelineMeasurements.add(measurement);
            }

            return timelineToMeasurements.entrySet();
        }

        private Number[][] getValues(List<Measurement> measurements, long diff) {
            Number[][] values = new Number[measurements.size()][2];
            for (int i = 0; i < measurements.size(); i++) {
                Measurement m = measurements.get(i);
                values[i][0] = m.getTimestamp().getTime() - diff;
                values[i][1] = m.getValue();
            }
            return values;
        }

        private long getDiff(Map.Entry<Timeline, List<Measurement>> timelineListEntry) {
            if (timelineListEntry.getKey().getScenarioId() != null) {
                Measurement first = timelineListEntry.getValue().get(0);
                return first.getTimestamp().getTime() - selectedExperiment.getStart().getTime();
            }
            return 0;
        }

        private ChartSeries createSeries(Timeline timeline) {
            Parameter parameter = timeline.getParameter();

            ChartSeries series = new ChartSeries();
            series.setDeviceId(parameter.getDeviceId());
            series.setParameterId(parameter.getId());
            series.setTimelineId(timeline.getId());

			series.setName(parameter.getDevice().getCustomId() + ": " + parameter.getParameterName() + " ("
					+ parameter.getMeasurementTypeName() + ")");
            series.setLabel(parameter.getMeasurementTypeName());
            series.setUnit(parameter.getMeasurementTypeUnit());

            return series;
        }

        @Override
        public void onError(ErrorDetails errorDetails) {
            eventBus.showError(errorDetails);
        }
    }

    public void setTimelines(List<Timeline> timelines) {
        getChart().setLoadingState(true);

        final Map<String, Timeline> idToRealTimeline = new HashMap<>();
        final Map<String, Timeline> idToScenarioTimeline = new HashMap<>();
        selectionManager.clear();
        idToDevice = new HashMap<>();
        for (Timeline timeline : timelines) {
            GWT.log("Showing timeline with " + timeline.getId() + "id belonging to " +
                    timeline.getContextId() + " and scenario " + timeline.getScenarioId());
            if (timeline.getScenarioId() != null) {
                idToScenarioTimeline.put(timeline.getId(), timeline);
            } else {
                idToRealTimeline.put(timeline.getId(), timeline);
            }

            Device d = timeline.getParameter().getDevice();
            selectionManager.add(d);
            idToDevice.put(d.getId(), d);
        }


        if (idToRealTimeline.size() > 0) {
            GWT.log("Loading real measurements");
            dapController.getMeasurements(idToRealTimeline.keySet(), selectedExperiment.getStart(),
                    selectedExperiment.getEnd(), new ChartPointMeasurementsCallback(idToRealTimeline, new ChartPointsCallback() {
                        @Override
                        public void processChartPoints(final List<ChartSeries> realTimelineToMeasurements) {
                            loadScenarioTimelines(realTimelineToMeasurements, idToScenarioTimeline);
                        }
                    }));
        } else {
            loadScenarioTimelines(new ArrayList<ChartSeries>(), idToScenarioTimeline);
        }
    }

    private pl.ismop.web.client.widgets.common.chart.ChartPresenter getChart() {
        if(chartPresenter == null) {
            chartPresenter = eventBus.addHandler(pl.ismop.web.client.widgets.common.chart.ChartPresenter.class);
            chartPresenter.setDeviceSelectHandler(new pl.ismop.web.client.widgets.common.chart.ChartPresenter.DeviceSelectHandler() {
                @Override
                public void select(ChartSeries series) {
                    selectionManager.select(getDevice(series));
                }

                @Override
                public void unselect(ChartSeries series) {
                    selectionManager.unselect(getDevice(series));
                }

                private Device getDevice(ChartSeries series) {
                    return idToDevice.get(series.getDeviceId());
                }
            });

            getView().setChart(chartPresenter.getView());
        }

        return chartPresenter;
    }

    public void onDateChanged(Date selectedDate) {
        getChart().selectDate(selectedDate, properties.selectionColor());
    }

    public void setWizard(ChartWizardPresenter wizard) {
        this.wizard = wizard;
    }

	private void loadScenarioTimelines(final List<ChartSeries> realTimelineToMeasurements,
	                                   final Map<String, Timeline> idToScenarioTimeline) {
	    if (idToScenarioTimeline.size() > 0) {
	        GWT.log("Loading scenarios measurements");
	        dapController.getAllMeasurements(idToScenarioTimeline.keySet(),
	                new ChartPointMeasurementsCallback(idToScenarioTimeline, new ChartPointsCallback() {
	                    @Override
	                    public void processChartPoints(List<ChartSeries> scenarioTimelineToMeasurements) {
	                        realTimelineToMeasurements.addAll(scenarioTimelineToMeasurements);
                            setSeries(realTimelineToMeasurements);
	                    }
	                }));
	    } else {
            setSeries(realTimelineToMeasurements);
	    }
	}

    private void setSeries(List<ChartSeries> series) {
        getChart().setLoadingState(false);
        chartPresenter.reset();
        for (ChartSeries s : series) {
            chartPresenter.addChartSeries(s);
        }
    }
}
