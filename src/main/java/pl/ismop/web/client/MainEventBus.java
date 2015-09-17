package pl.ismop.web.client;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.InitHistory;
import com.mvp4g.client.event.EventBusWithLookup;

import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.error.ErrorPresenter;
import pl.ismop.web.client.widgets.monitoring.fibre.FibrePresenter;
import pl.ismop.web.client.widgets.monitoring.mapnavigator.LeveeNavigatorPresenter;
import pl.ismop.web.client.widgets.monitoring.sidepanel.MonitoringSidePanelPresenter;
import pl.ismop.web.client.widgets.monitoring.weather.WeatherStationPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;

@Events(startPresenter = RootPresenter.class, historyOnStart = true)
public interface MainEventBus extends EventBusWithLookup {
	@InitHistory
	@Event(handlers = RootPresenter.class, historyConverter = MenuHistoryConverter.class)
	void monitoringPanel();
	
	@Event(handlers = RootPresenter.class, historyConverter = MenuHistoryConverter.class)
	void analysisPanel();

	@Event(handlers = ErrorPresenter.class)
	void showError(ErrorDetails errorDetails);

	@Event(handlers = WeatherStationPresenter.class)
	void showWeatherPanel();

	@Event(handlers = FibrePresenter.class)
	void showFibrePanel(Levee levee);

	@Event(handlers = MonitoringSidePanelPresenter.class)
	void leveeNavigatorReady();

	@Event(handlers = LeveeNavigatorPresenter.class)
	void leveeSelected(Levee levee);

	@Event(handlers = MonitoringSidePanelPresenter.class)
	void showProfileMetadata(Profile profile, boolean show);

	@Event(handlers = MonitoringSidePanelPresenter.class)
	void showSectionMetadata(Section section, boolean show);
	
	@Event(handlers = MonitoringSidePanelPresenter.class)
	void showDeviceMetadata(Device device, boolean show);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void profileClicked(Profile profile);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void sectionClicked(Section section);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void deviceClicked(Device device);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void deviceAggregateClicked(DeviceAggregation deviceAggregation);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void zoomOut(String sectionId);

	@Event(handlers = MonitoringSidePanelPresenter.class)
	void deviceSelected(Device device, boolean selected);
}