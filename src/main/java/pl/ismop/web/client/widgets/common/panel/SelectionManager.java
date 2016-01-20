package pl.ismop.web.client.widgets.common.panel;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.geojson.MapFeature;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by marek on 07.10.15.
 */
public class SelectionManager implements ISelectionManager {

    private final MainEventBus eventBus;
    private Set<MapFeature> mapFeatures = new HashSet<>();

    public SelectionManager(MainEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void select(MapFeature mapFeature) {
        mapFeatures.add(mapFeature);
        eventBus.select(mapFeature);
    }

    @Override
    public void unselect(MapFeature mapFeature) {
        mapFeatures.remove(mapFeature);
        eventBus.unselect(mapFeature);
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
        mapFeatures.clear();
        eventBus.clearMinimap();
    }

    public void activate() {
        for (MapFeature mapFeature : mapFeatures) {
            eventBus.select(mapFeature);
        }
    }

    public void deactivate() {
        eventBus.clearMinimap();
    }
}
