package pl.ismop.web.client.widgets.analysis.chart;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.IsmopProperties;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.timeline.Timeline;
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
    private final IsmopConverter formatter;
    private Experiment selectedExperiment;
    private List<Timeline> timelines;
    ChartMessages messages;
    private ISelectionManager selectionManager;
    private ChartWizardPresenter wizard;

    @Inject
    public ChartPresenter(DapController dapController, IsmopProperties properties, IsmopConverter formatter) {
        this.dapController = dapController;
        this.properties = properties;
        this.formatter = formatter;
    }

    @Override
    public void bind() {
        messages = getView().getMessages();
    }

    @Override
    public void setSelectedExperiment(Experiment experiment) {
        selectedExperiment = experiment;
        getView().setInterval(experiment.getStartDate(), experiment.getEndDate());
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

    public void setTimelines(List<Timeline> timelines) {
        getView().showLoading(messages.loadingMeasurements());

        this.timelines = timelines;
        final Map<String, Timeline> idToTimeline = new HashMap<>();
        selectionManager.clear();
        for (Timeline timeline : timelines) {
            idToTimeline.put(timeline.getId(), timeline);
            selectionManager.selectDevice(timeline.getParameter().getDevice());
        }

        dapController.getMeasurements(idToTimeline.keySet(), selectedExperiment.getStartDate(),
                selectedExperiment.getEndDate(), new DapController.MeasurementsCallback() {
            @Override
            public void processMeasurements(List<Measurement> measurements) {
                getView().setSeries(map(measurements));
            }

            private Map<Timeline, List<DateChartPoint>> map(List<Measurement> measurements) {
                Map<Timeline, List<DateChartPoint>> timelineToMeasurements = new HashMap<>();
                for (Measurement measurement : measurements) {
                    Timeline timeline = idToTimeline.get(measurement.getTimelineId());
                    List<DateChartPoint> timelineMeasurements = timelineToMeasurements.get(timeline);
                    if(timelineMeasurements == null) {
                        timelineMeasurements = new ArrayList<>();
                        timelineToMeasurements.put(timeline, timelineMeasurements);
                    }

                    Date date = formatter.parse(measurement.getTimestamp());
                    timelineMeasurements.add(new DateChartPoint(date, measurement.getValue()));
                }

                return timelineToMeasurements;
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });
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
