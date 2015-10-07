package pl.ismop.web.client.widgets.analysis.dummy;

import com.google.gwt.user.client.Window;
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

    // dummy 2 devices with hardcoded ids, just for tests.
    private List<Device> devices;

    // dummy 2 sections with hardcoded ids, just for tests.
    private List<Section> sections;
    // dummy 2 profiles with hardcoded ids, just for tests.
    private List<Profile> profiles;

    @Inject
    public DummyPresenter(DapController dapController) {
        this.dapController = dapController;
    }

    @Override
    public void bind() {
        dapController.getDevices(Arrays.asList("100", "1264"), new DapController.DevicesCallback() {
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
        Window.alert("Edit this view");
    }

    @Override
    public void setSelectionManager(ISelectionManager selectionManager) {
        this.selectionManager = selectionManager;
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
    public void showDevice1() {
        selectionManager.showDevice(devices.get(0));
    }

    @Override
    public void showDevice2() {
        selectionManager.showDevice(devices.get(1));
    }

    @Override
    public void selectDevice1() {
        selectionManager.selectDevice(devices.get(0));
    }

    @Override
    public void unselectDevice1() {
        selectionManager.unselectDevice(devices.get(0));
    }

    @Override
    public void selectDevice2() {
        selectionManager.selectDevice(devices.get(1));
    }

    @Override
    public void unselectDevice2() {
        selectionManager.unselectDevice(devices.get(1));
    }

    @Override
    public void showSection1() {
        selectionManager.showSection(sections.get(0));
    }

    @Override
    public void showSection2() {
        selectionManager.showSection(sections.get(1));
    }

    @Override
    public void showProfile1() {
        selectionManager.showProfile(profiles.get(0));
    }

    @Override
    public void showProfile2() {
        selectionManager.showProfile(profiles.get(1));
    }
}
