package pl.ismop.web.client.widgets.common.panel;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by marek on 07.10.15.
 */
public class SelectionManager implements ISelectionManager {

    private final MainEventBus eventBus;
    private Set<Device> selectedDevices = new HashSet<>();

    public SelectionManager(MainEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void select(Device device) {
        selectedDevices.add(device);
        eventBus.selectDevice(device);
    }

    @Override
    public void unselect(Device device) {
        selectedDevices.remove(device);
        eventBus.unselectDevice(device);
    }

    @Override
    public void show(Device device) {
        eventBus.showDevice(device);
    }

    @Override
    public void show(Profile profile) {
        eventBus.showProfile(profile);
    }

    @Override
    public void clear() {
        selectedDevices.clear();
        eventBus.clearMinimap();
    }

    public void activate() {
        for (Device selectedDevice : selectedDevices) {
            eventBus.selectDevice(selectedDevice);
        }
    }

    public void deactivate() {
        eventBus.clearMinimap();
    }
}
