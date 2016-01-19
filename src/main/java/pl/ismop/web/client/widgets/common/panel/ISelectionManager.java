package pl.ismop.web.client.widgets.common.panel;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;

/**
 * Created by marek on 07.10.15.
 */
public interface ISelectionManager {
    /**
    * Select device on minima. Many devices can be selected on minimap (yellow marker will be used).
    * To unselect device use {@link #unselect(pl.ismop.web.client.dap.device.Device)}.
    *
    * @param device Device to be selected.
    */
    void select(Device device);

    /**
     * Unselect device. To select device use {@link #select(Device)}.
     *
     * @param device Device to be unselected
     */
    void unselect(Device device);

    /**
     * Show device. Only one device can be shown on minimap in the same time (red marker will be used).
     *
     * @param device Device to be shown.
     */
    void show(Device device);

    /**
     * Show section. Only one section can be shown on minimap in the same time.
     *
     * @param section Section to be shown.
     */
    void show(Section section);

    /**
     * Show profile. Only one profile can be shown on minimap in the same time.
     *
     * @param profile Profile to be shown.
     */
    void show(Profile profile);

    void clear();
}
