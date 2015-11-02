package pl.ismop.web.client;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.InitHistory;
import com.mvp4g.client.event.EventBusWithLookup;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.chart.ChartPresenter;
import pl.ismop.web.client.widgets.analysis.chart.wizard.ChartWizardPresenter;
import pl.ismop.web.client.widgets.analysis.comparison.ComparisonPresenter;
import pl.ismop.web.client.widgets.analysis.dummy.DummyPresenter;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSlicePresenter;
import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.HorizontalSliceWizardPresenter;
import pl.ismop.web.client.widgets.analysis.sidepanel.AnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSlicePresenter;
import pl.ismop.web.client.widgets.analysis.verticalslice.wizard.VerticalSliceWizardPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.error.ErrorPresenter;
import pl.ismop.web.client.widgets.monitoring.fibre.FibrePresenter;
import pl.ismop.web.client.widgets.monitoring.mapnavigator.LeveeNavigatorPresenter;
import pl.ismop.web.client.widgets.monitoring.readings.ReadingsPresenter;
import pl.ismop.web.client.widgets.monitoring.sidepanel.MonitoringSidePanelPresenter;
import pl.ismop.web.client.widgets.monitoring.weather.WeatherStationPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;

import java.util.Date;
import java.util.List;

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
	
	@Event(handlers = MonitoringSidePanelPresenter.class)
	void showDeviceAggregateMetadata(DeviceAggregate deviceAggregate, boolean show);

	@Event(handlers = {LeveeNavigatorPresenter.class, HorizontalSliceWizardPresenter.class, VerticalSliceWizardPresenter.class})
	void profileClicked(Profile profile);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void sectionClicked(Section section);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void deviceClicked(Device device);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void deviceAggregateClicked(DeviceAggregate deviceAggregation);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void zoomOut(String sectionId);

	@Event(handlers = MonitoringSidePanelPresenter.class)
	void deviceSelected(Device device, boolean selected);

	@Event(handlers = ReadingsPresenter.class)
	void showExpandedReadings(Levee levee, List<ChartSeries> series);

	@Event(handlers = ReadingsPresenter.class)
	void deviceSeriesHover(String deviceId, boolean hover);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void backFromSideProfile();

	@Event(handlers = LeveeNavigatorPresenter.class)
	void devicesHovered(List<String> deviceIds, boolean hovered);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void devicesClicked(List<String> deviceIds);

	@Event(handlers = LeveeNavigatorPresenter.class)
	void clearSelection();

	@Event(handlers = ComparisonPresenter.class)
	void addPanel(String panelTitle, IPanelContent<?, ?> content);

	@Event(handlers = { DummyPresenter.class, AnalysisSidePanelPresenter.class, ChartPresenter.class, HorizontalSlicePresenter.class})
	void dateChanged(Date selectedDate);

	@Event(handlers = { DummyPresenter.class, ComparisonPresenter.class })
	void experimentChanged(Experiment selectedExperiment);

	/**
	 * Select device on minima. Many devices can be selected on minimap (yellow marker will be used).
	 * To unselect device use {@link #unselectDevice(Device)}.
	 *
	 * @param device Device to be selected.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void selectDevice(Device device);

	/**
	 * Unselect device. To select device use {@link #selectDevice(Device)}.
	 *
	 * @param device Device to be unselected
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void unselectDevice(Device device);

	/**
	 * Show device. Only one device can be shown on minimap in the same time (red marker will be used).
	 *
	 * @param device Device to be shown.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void showDevice(Device device);

	/**
	 * Show section. Only one section can be shown on minimap in the same time.
	 *
	 * @param section Section to be shown.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void showSection(Section section);

	/**
	 * Show profile. Only one profile can be shown on minimap in the same time.
	 *
	 * @param profile Profile to be shown.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void showProfile(Profile profile);

	/**
	 * Remove all selections and shows from the minimap.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void clearMinimap();

	@Event(handlers = ChartWizardPresenter.class)
	void timelineSelectionChanged();

	@Event(handlers = HorizontalSliceWizardPresenter.class, activate = HorizontalSliceWizardPresenter.class,
			deactivate = {LeveeNavigatorPresenter.class, VerticalSliceWizardPresenter.class})
	void showHorizontalCrosssectionWizard();

	@Event(handlers = HorizontalSliceWizardPresenter.class, activate = HorizontalSliceWizardPresenter.class,
			deactivate = {LeveeNavigatorPresenter.class, VerticalSliceWizardPresenter.class})
	void showHorizontalCrosssectionWizardWithConfig(HorizontalCrosssectionConfiguration configuration);

	@Event(handlers = HorizontalSlicePresenter.class)
	void updateHorizontalSliceConfiguration(HorizontalCrosssectionConfiguration configuration);

	@Event(activate = {LeveeNavigatorPresenter.class, VerticalSliceWizardPresenter.class}, deactivate = HorizontalSliceWizardPresenter.class)
	void horizontalCrosssectionWizardHidden();

	@Event(handlers = VerticalSliceWizardPresenter.class, activate = VerticalSliceWizardPresenter.class,
			deactivate = {LeveeNavigatorPresenter.class, HorizontalSliceWizardPresenter.class})
	void showVerticalCrosssectionWizard();
	
	@Event(handlers = VerticalSliceWizardPresenter.class, activate = VerticalSliceWizardPresenter.class,
			deactivate = {LeveeNavigatorPresenter.class, HorizontalSliceWizardPresenter.class})
	void showVerticalCrosssectionWizardWithConfig(VerticalCrosssectionConfiguration configuration);

	@Event(handlers = VerticalSlicePresenter.class)
	void updateVerticalSliceConfiguration(VerticalCrosssectionConfiguration configuration);
	
	@Event(activate = {LeveeNavigatorPresenter.class, HorizontalSliceWizardPresenter.class}, deactivate = VerticalSliceWizardPresenter.class)
	void verticalCrosssectionWizardHidden();
}