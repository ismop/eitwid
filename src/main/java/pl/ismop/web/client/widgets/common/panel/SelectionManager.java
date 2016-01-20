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
    private Set<MapFeature> selected = new HashSet<>();
    private Set<MapFeature> highlighted = new HashSet<>();
    private Set<MapFeature> added = new HashSet<>();

    public SelectionManager(MainEventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public void add(MapFeature mapFeature) {
        added.add(mapFeature);
        eventBus.add(mapFeature);
    }

    @Override
    public void rm(MapFeature mapFeature) {
        added.remove(mapFeature);
        eventBus.rm(mapFeature);
    }

    @Override
    public void select(MapFeature mapFeature) {
        selected.add(mapFeature);
        eventBus.select(mapFeature);
    }

    @Override
    public void unselect(MapFeature mapFeature) {
        selected.remove(mapFeature);
        eventBus.unselect(mapFeature);
    }

    @Override
    public void highlight(MapFeature mapFeature) {
        highlighted.add(mapFeature);
        eventBus.highlight(mapFeature);
    }

    @Override
    public void unhighlight(MapFeature mapFeature) {
        highlighted.remove(mapFeature);
        eventBus.unhighlight(mapFeature);
    }

    @Override
    public void clear() {
        selected.clear();
        eventBus.clearMinimap();
    }

    public void activate() {
        for (MapFeature mapFeature : added) {
            eventBus.add(mapFeature);
        }

        for (MapFeature mapFeature : selected) {
            eventBus.select(mapFeature);
        }

        for (MapFeature mapFeature : highlighted) {
            eventBus.highlight(mapFeature);
        }
    }

    public void deactivate() {
        eventBus.clearMinimap();
    }
}
