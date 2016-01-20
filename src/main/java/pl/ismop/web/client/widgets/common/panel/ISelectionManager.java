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
     * Add map feature into minimap. To remove map feature use {@link #rm(MapFeature)}.
     *
     * @param mapFeature Map feature to be added.
     */
    void add(MapFeature mapFeature);

    /**
     * Remove map feature from minimap. To add map feature use {@link #add(MapFeature)}.
     *
     * @param mapFeature Map feature to be removed.
     */
    void rm(MapFeature mapFeature);

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
     * Highlight map feature on minimap. To unhighlight map feature use {@link #unhighlight(MapFeature)}.
     *
     * @param mapFeature Map feature to be highlighted.
     */
    void highlight(MapFeature mapFeature);

    /**
     * Unhighlight map feature on minimap. To highlight map feature use {@link #highlight(MapFeature)}.
     *
     * @param mapFeature Map feature to be unhighlighted.
     */
    void unhighlight(MapFeature mapFeature);

    void clear();
}
