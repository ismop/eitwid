package pl.ismop.web.client.widgets.analysis.chart;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.IsmopProperties;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorCallback;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.chart.wizard.ChartWizardPresenter;
import pl.ismop.web.client.widgets.common.DateChartPoint;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

import java.util.*;

@Presenter(view = ChartView.class, multiple = true)
public class ChartPresenter extends BasePresenter<IChartView, MainEventBus>
        implements IPanelContent<IChartView, MainEventBus>, IChartView.IChartPresenter {
    private final DapController dapController;
    private final IsmopProperties properties;
    private Experiment selectedExperiment;
    private List<Timeline> timelines;
    ChartMessages messages;
    private ISelectionManager selectionManager;
    private ChartWizardPresenter wizard;

    @Inject
    public ChartPresenter(DapController dapController, IsmopProperties properties) {
        this.dapController = dapController;
        this.properties = properties;
    }

    @Override
    public void bind() {
        messages = getView().getMessages();
    }

    @Override
    public void setSelectedExperiment(Experiment experiment) {
        selectedExperiment = experiment;
        getView().setInterval(experiment.getStart(), experiment.getEnd());
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
        void processChartPoints(Map<Timeline, List<DateChartPoint>> timelineToMeasurements);
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

        private Map<Timeline, List<DateChartPoint>> map(List<Measurement> measurements) {
            Map<Timeline, List<DateChartPoint>> timelineToMeasurements = new HashMap<>();
            Map<Timeline, Long> diffs = new HashMap<>();

            for (Measurement measurement : measurements) {
                Timeline timeline = idToTimeline.get(measurement.getTimelineId());
                List<DateChartPoint> timelineMeasurements = timelineToMeasurements.get(timeline);
                long diff = 0;
                if (timelineMeasurements == null) {
                    timelineMeasurements = new ArrayList<>();
                    timelineToMeasurements.put(timeline, timelineMeasurements);
                }

                if (timeline.getScenarioId() != null) {
                    if (!diffs.containsKey(timeline)) {

                        diffs.put(timeline, measurement.getTimestamp().getTime() -
                                selectedExperiment.getStart().getTime());
                    }
                    diff = diffs.get(timeline);
                }

                timelineMeasurements.add(new DateChartPoint(new Date(measurement.getTimestamp().getTime() - diff),
                        measurement.getValue()));
            }

            return timelineToMeasurements;
        }

        @Override
        public void onError(ErrorDetails errorDetails) {
            eventBus.showError(errorDetails);
        }
    }

    public void setTimelines(List<Timeline> timelines) {
        getView().showLoading(messages.loadingMeasurements());

        this.timelines = timelines;
        final Map<String, Timeline> idToRealTimeline = new HashMap<>();
        final Map<String, Timeline> idToScenarioTimeline = new HashMap<>();
        selectionManager.clear();
        for (Timeline timeline : timelines) {
            GWT.log("Showing timeline with " + timeline.getId() + "id belonging to " +
                    timeline.getContextId() + " and scenario " + timeline.getScenarioId());
            if (timeline.getScenarioId() != null) {
                idToScenarioTimeline.put(timeline.getId(), timeline);
            } else {
                idToRealTimeline.put(timeline.getId(), timeline);
            }
            selectionManager.selectDevice(timeline.getParameter().getDevice());
        }


        if (idToRealTimeline.size() > 0) {
            GWT.log("Loading real measurements");
            dapController.getMeasurements(idToRealTimeline.keySet(), selectedExperiment.getStart(),
                    selectedExperiment.getEnd(), new ChartPointMeasurementsCallback(idToRealTimeline, new ChartPointsCallback() {
                        @Override
                        public void processChartPoints(final Map<Timeline, List<DateChartPoint>> realTimelineToMeasurements) {
                            loadScenarioTimelines(realTimelineToMeasurements, idToScenarioTimeline);
                        }
                    }));
        } else {
            loadScenarioTimelines(new HashMap<Timeline, List<DateChartPoint>>(), idToScenarioTimeline);
        }
    }

    private void loadScenarioTimelines(final Map<Timeline, List<DateChartPoint>> realTimelineToMeasurements,
                                       final Map<String, Timeline> idToScenarioTimeline) {
        if (idToScenarioTimeline.size() > 0) {
            GWT.log("Loading scenarios measurements");
            dapController.getAllMeasurements(idToScenarioTimeline.keySet(),
                    new ChartPointMeasurementsCallback(idToScenarioTimeline, new ChartPointsCallback() {
                        @Override
                        public void processChartPoints(Map<Timeline, List<DateChartPoint>> scenarioTimelineToMeasurements) {
                            realTimelineToMeasurements.putAll(scenarioTimelineToMeasurements);
                            getView().setSeries(realTimelineToMeasurements);
                        }
                    }));
        } else {
            getView().setSeries(realTimelineToMeasurements);
        }
    }

    @SuppressWarnings("unused")
    public void onDateChanged(Date selectedDate) {
        getView().selectDate(selectedDate, properties.selectionColor());
    }

    public void setWizard(ChartWizardPresenter wizard) {
        this.wizard = wizard;
    }

    @Override
    public void timelineSelected(Timeline timeline) {
        selectionManager.showDevice(timeline.getParameter().getDevice());
    }
}
