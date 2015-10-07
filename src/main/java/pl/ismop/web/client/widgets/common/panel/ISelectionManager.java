package pl.ismop.web.client.widgets.common.panel;

import com.mvp4g.client.annotation.Event;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.widgets.analysis.sidepanel.AnalysisSidePanelPresenter;

/**
 * Created by marek on 07.10.15.
 */
public interface ISelectionManager {
    /**
    * Select device on minima. Many devices can be selected on minimap (yellow marker will be used).
    * To unselect device use {@link #unselectDevice(pl.ismop.web.client.dap.device.Device)}.
    *
    * @param device Device to be selected.
    */
    void selectDevice(Device device);

    /**
     * Unselect device. To select device use {@link #selectDevice(Device)}.
     *
     * @param device Device to be unselected
     */
    void unselectDevice(Device device);

    /**
     * Show device. Only one device can be shown on minimap in the same time (red marker will be used).
     *
     * @param device Device to be shown.
     */
    void showDevice(Device device);

    /**
     * Show section. Only one section can be shown on minimap in the same time.
     *
     * @param section Section to be shown.
     */
    void showSection(Section section);

    /**
     * Show profile. Only one profile can be shown on minimap in the same time.
     *
     * @param profile Profile to be shown.
     */
    void showProfile(Profile profile);
}
