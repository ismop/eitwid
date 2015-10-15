package pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel.ISensorPanelView.ISensorPanelPresenter;
import pl.ismop.web.client.widgets.delegator.ContextsCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Presenter(view = SensorPanelView.class, multiple = true)
public class SensorPanelPresenter extends BasePresenter<ISensorPanelView, MainEventBus>
        implements ISensorPanelPresenter {
    private Parameter parameter;
    private SensorPanelMessages messages;
    private DapController dapController;
    private Map<String, Timeline> timelineNamesToTimeline;

    @Inject
    SensorPanelPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    @Override
    public void bind() {
        messages = getView().getMessages();
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
        getView().setHeaderTitle(parameter.getDevice().getCustomId() + " (" + parameter.getMeasurementTypeName() + ")");

        loadTimelines();
    }

    public Parameter getParameter() {
        return parameter;
    }

    private void loadTimelines() {
        getView().setLoading(messages.loadingTimelines());
        dapController.getParameterTimelines(parameter.getId(), new DapController.TimelinesCallback() {
            @Override
            public void processTimelines(final List<Timeline> timelines) {
                timelineNamesToTimeline = new HashMap<>();
                List<String> contextIds = new ArrayList<>();
                for (Timeline timeline : timelines) {
                    contextIds.add(timeline.getContextId());
                }

                getView().setLoading(messages.loadingContexts());
                dapController.getContexts(contextIds, new ContextsCallback(this) {
                    @Override
                    public void processContexts(List<Context> contexts) {
                        Map<String, Context> idToContext = new HashMap<>();
                        for (Context context : contexts) {
                            idToContext.put(context.getId(), context);
                        }

                        for (Timeline timeline : timelines) {
                            // TODO: better names for timelines. Take into account also scenarios
                            // when added to DAP
                            String name = idToContext.get(timeline.getContextId()).getName();
                            timeline.setLabel(name);
                            timeline.setParameter(parameter);
                            timelineNamesToTimeline.put(name, timeline);
                        }
                        getView().setTimelines(timelineNamesToTimeline.keySet());
                    }
                });
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });
    }

    public List<Timeline> getSelectedTimelines() {
        List<String> selectedNames = getView().getSelected();
        List<Timeline> selectedTimelines = new ArrayList<>();
        for (String selectedName : selectedNames) {
            selectedTimelines.add(timelineNamesToTimeline.get(selectedName));
        }

        return selectedTimelines;
    }

    @Override
    public void timelineSelectionChanged() {
        eventBus.timelineSelectionChanged();
    }
}
