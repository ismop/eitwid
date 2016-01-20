package pl.ismop.web.client.widgets.common.panel;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.geojson.MapFeature;

/**
 * Created by marek on 07.10.15.
 */
public interface ISelectionManager {
    /**
    * Select map feature on minimap. If map feature was not added to the map is will be added at the
    * beginning and next selected. To unselect map feature use {@link #unselect(MapFeature)}.
    *
    * @param mapFeature Map feature to be selected.
    */
    void select(MapFeature mapFeature);

    /**
     * Unselect map feature on minimap. To select device use {@link #select(MapFeature)}.
     *
     * @param mapFeature Map feature to be unselected
     */
    void unselect(MapFeature mapFeature);

    /**
     * Show device. Only one device can be shown on minimap in the same time (red marker will be used).
     *
     * @param device Device to be shown.
     */
    void show(Device device);

    /**
     * Show profile. Only one profile can be shown on minimap in the same time.
     *
     * @param profile Profile to be shown.
     */
    void show(Profile profile);

    void clear();
}
