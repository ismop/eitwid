package pl.ismop.web.client.widgets.analysis.chart.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.chart.wizard.IChartWizardView.IChartWizardPresenter;
import pl.ismop.web.client.widgets.analysis.chart.wizard.sensorpanel.SensorPanelPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.delegator.ParametersCallback;

@Presenter(view = ChartWizardView.class, multiple = true)
public class ChartWizardPresenter extends BasePresenter<IChartWizardView, MainEventBus>
        implements IChartWizardPresenter {
    private Experiment selectedExperiment;
    private ShowResult showResult;
    private MapPresenter miniMap;
    private ChartWizardMessages messages;
    private Map<String, Parameter> nameToParameter;
    private Map<String, SensorPanelPresenter> panels = new HashMap<>();

    public interface ShowResult {
        void ok(List<Timeline> selectedTimelines);
    }

    private final DapController dapController;

    @Inject
    public ChartWizardPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    public void show(Experiment selectedExperiment, ShowResult showResult) {
        messages = getView().getMessages();
        this.selectedExperiment = selectedExperiment;
        this.showResult = showResult;
        getView().show();
    }

    @Override
    public void onModalReady() {
        if (miniMap == null) {
            miniMap = eventBus.addHandler(MapPresenter.class);
            view.setMiniMap(miniMap.getView());
            for (Section section : selectedExperiment.getSections()) {
                GWT.log(section.getId());
                miniMap.add(section);
            }
        }

        if (nameToParameter == null) {
            loadDevices();
        }
    }

    private void loadDevices() {
        getView().setLoading(" " + messages.loadingDevices());
        dapController.getLeveeDevices(selectedExperiment.getLeveeId(), new DapController.DevicesCallback() {
            @Override
            public void processDevices(List<Device> devices) {
                List<String> deviceIds = new ArrayList<>();
                final Map<String, Device> idToDevice = new HashMap<>();
                for (Device device : devices) {
                    deviceIds.add(device.getId());
                    idToDevice.put(device.getId(), device);

                }

                getView().setLoading(" " + messages.loadingParameters());
                dapController.getLeveeParameters(selectedExperiment.getLeveeId(), new ParametersCallback(this) {
                    @Override
                    public void processParameters(List<Parameter> parameters) {
                        nameToParameter = new HashMap<>();
                        for (Parameter parameter : parameters) {
                            parameter.setDevice(idToDevice.get(parameter.getDeviceId()));
                            if (parameter.getDevice() != null) {
                                nameToParameter.put(parameterName(parameter), parameter);
                            } else {
                                GWT.log("Warning: " + parameter.getId() + " parameter without device assigned");
                            }
                        }

                        showDevices();
                    }
                });
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });
    }

    private void showDevices() {
        getView().setDevices(nameToParameter.keySet());
    }

    @Override
    public void modalCanceled() {
        getView().close();
    }

    @Override
    public void modalOk() {
        showResult.ok(getSelectedTimelines());
        getView().close();
    }

    public void destroy() {
        eventBus.removeHandler(this);
        getView().removeFromParent();
    }

    @Override
    public void addParameter(String parameterName) {
        Parameter parameter = nameToParameter.get(parameterName);
        SensorPanelPresenter panelPresenter = eventBus.addHandler(SensorPanelPresenter.class);
        panelPresenter.setParameter(parameter);
        panelPresenter.setSelectedExperiment(selectedExperiment);
        getView().addPanel(panelPresenter.getView());

        panels.put(parameterName, panelPresenter);
        miniMap.add(parameter.getDevice());
    }

    @Override
    public void removeParameter(String parameterName) {
        SensorPanelPresenter panel = panels.get(parameterName);
        if (panel != null) {
            removePanel(parameterName);
            miniMap.rm(panel.getParameter().getDevice());
        }
    }

    private void removePanel(String parameterName) {
        SensorPanelPresenter panel = panels.remove(parameterName);
        eventBus.removeHandler(panel);

        getView().removePanel(panel.getView());
    }

    private String parameterName(Parameter parameter) {
        return parameter.getDevice().getCustomId() + ": " + parameter.getParameterName() + " (" + parameter.getMeasurementTypeName() + ")";
    }

    @SuppressWarnings("unused")
    public void onTimelineSelectionChanged() {
        getView().setOkEnabled(getSelectedTimelines().size() > 0);
    }

    public List<Timeline> getSelectedTimelines() {
        List<Timeline> selectedTimelines = new ArrayList<>();
        for (SensorPanelPresenter panel : panels.values()) {
            selectedTimelines.addAll(panel.getSelectedTimelines());
        }
        return selectedTimelines;
    }
}
