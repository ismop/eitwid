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
import pl.ismop.web.client.widgets.analysis.comparison.IPanelContent;
import pl.ismop.web.client.widgets.analysis.dummy.IDummyView.IDumyPresenter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Presenter(view = DummyView.class, multiple = true)
public class DummyPresenter extends BasePresenter<IDummyView, MainEventBus>
        implements IPanelContent<IDummyView, MainEventBus>, IDumyPresenter {
    private final DapController dapController;
    Date date;
    Experiment experiment;

    // dummy 2 devices with hardcoded ids, just for tests.
    List<Device> devices;

    // dummy 2 sections with hardcoded ids, just for tests.
    List<Section> sections;

    // dummy 2 profiles with hardcoded ids, just for tests.
    List<Profile> profiles;

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
        eventBus.showDevice(devices.get(0));
    }

    @Override
    public void showDevice2() {
        eventBus.showDevice(devices.get(1));
    }

    @Override
    public void selectDevice1() {
        eventBus.selectDevice(devices.get(0));
    }

    @Override
    public void unselectDevice1() {
        eventBus.unselectDevice(devices.get(0));
    }

    @Override
    public void selectDevice2() {
        eventBus.selectDevice(devices.get(1));
    }

    @Override
    public void unselectDevice2() {
        eventBus.unselectDevice(devices.get(1));
    }

    @Override
    public void showSection1() {
        eventBus.showSection(sections.get(0));
    }

    @Override
    public void showSection2() {
        eventBus.showSection(sections.get(1));
    }

    @Override
    public void showProfile1() {
        eventBus.showProfile(profiles.get(0));
    }

    @Override
    public void showProfile2() {
        eventBus.showProfile(profiles.get(1));
    }

    @Override
    public void clearMinimap() {
        eventBus.clearMinimap();
    }
}
