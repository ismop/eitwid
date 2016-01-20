package pl.ismop.web.client.widgets.analysis.dummy;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.analysis.dummy.IDummyView.IDumyPresenter;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Presenter(view = DummyView.class, multiple = true)
public class DummyPresenter extends BasePresenter<IDummyView, MainEventBus>
        implements IPanelContent<IDummyView, MainEventBus>, IDumyPresenter {
    private final DapController dapController;
    private ISelectionManager selectionManager;
    private Date date;
    private Experiment experiment;

    private List<Device> devices;
    private List<Section> sections;
    private List<Profile> profiles;

    @Inject
    public DummyPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    @Override
    public void bind() {
        dapController.getDevices(Arrays.asList("1264"), new DapController.DevicesCallback() {
            @Override
            public void processDevices(List<Device> devices) {
                DummyPresenter.this.devices = devices;
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });

        dapController.getSections(Arrays.asList("1", "2"), new DapController.SectionsCallback() {
            @Override
            public void processSections(List<Section> sections) {
                DummyPresenter.this.sections = sections;
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });

        dapController.getProfiles(Arrays.asList("8", "9"), new DapController.ProfilesCallback() {
            @Override
            public void processProfiles(List<Profile> profiles) {
                DummyPresenter.this.profiles = profiles;
            }

            @Override
            public void onError(ErrorDetails errorDetails) {
                eventBus.showError(errorDetails);
            }
        });
    }

    @Override
    public void setSelectedExperiment(Experiment experiment) {
        this.experiment = experiment;
        updateTitle();
    }

    @Override
    public void setSelectedDate(Date date) {
        this.date = date;
        updateTitle();
    }

    @Override
    public void edit() {
    	eventBus.showSimpleError("Edit this view");
    }

    @Override
    public void setSelectionManager(ISelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @Override
    public void destroy() {

    }

    private void updateTitle() {
        String title = "";
        if (experiment != null) {
            title = title + "Selected experiment " + experiment.getName() + " ";
        }

        if (date != null) {
            title = title + "Selected time: " + date;
        }

        getView().setTitle(title);
    }

    @SuppressWarnings("unused")
    public void onDateChanged(Date selectedDate) {
        setSelectedDate(selectedDate);
    }

    @SuppressWarnings("unused")
    public void onExperimentChanged(Experiment selectedExperiment) {
        setSelectedExperiment(selectedExperiment);
    }


    @Override
    public void addDevice() {
        selectionManager.add(devices.get(0));
    }

    @Override
    public void rmDevice() {
        selectionManager.rm(devices.get(0));
    }

    @Override
    public void addSection() {
        selectionManager.add(sections.get(0));
    }

    @Override
    public void rmSection() {
        selectionManager.rm(sections.get(0));
    }

    @Override
    public void addProfile() {
        selectionManager.add(profiles.get(0));
    }

    @Override
    public void rmProfile() {
        selectionManager.rm(profiles.get(0));
    }

    @Override
    public void selectDevice() {
        selectionManager.select(devices.get(0));
    }

    @Override
    public void unselectDevice() {
        selectionManager.unselect(devices.get(0));
    }

    @Override
    public void selectSection() {
        selectionManager.select(sections.get(0));
    }

    @Override
    public void unselectSection() {
        selectionManager.unselect(sections.get(0));
    }

    @Override
    public void selectProfile() {
        selectionManager.select(profiles.get(0));
    }

    @Override
    public void unselectProfile() {
        selectionManager.unselect(profiles.get(0));
    }

    @Override
    public void highlightDevice() {
        selectionManager.highlight(devices.get(0));
    }

    @Override
    public void unhighlightDevice() {
        selectionManager.unhighlight(devices.get(0));
    }

    @Override
    public void highlightSection() {
        selectionManager.highlight(sections.get(0));
    }

    @Override
    public void unhighlightSection() {
        selectionManager.unhighlight(sections.get(0));
    }

    @Override
    public void highlightProfile() {
        selectionManager.highlight(profiles.get(0));
    }

    @Override
    public void unhighlightProfile() {
        selectionManager.unhighlight(profiles.get(0));
    }
}
