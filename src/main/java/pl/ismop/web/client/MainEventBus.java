package pl.ismop.web.client;

import java.util.Date;
import java.util.List;

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
import pl.ismop.web.client.geojson.MapFeature;
import pl.ismop.web.client.widgets.analysis.chart.ChartPresenter;
import pl.ismop.web.client.widgets.analysis.chart.wizard.ChartWizardPresenter;
import pl.ismop.web.client.widgets.analysis.comparison.ComparisonPresenter;
import pl.ismop.web.client.widgets.analysis.dummy.DummyPresenter;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSlicePresenter;
import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.HorizontalSliceWizardPresenter;
import pl.ismop.web.client.widgets.analysis.sidepanel.AnalysisSidePanelPresenter;
import pl.ismop.web.client.widgets.analysis.threatlevels.ThreatLevelsPresenter;
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
import pl.ismop.web.client.widgets.monitoring.waterheight.WaterHeightPresenter;
import pl.ismop.web.client.widgets.monitoring.weather.WeatherStationPresenter;
import pl.ismop.web.client.widgets.realtime.main.RealTimePanelPresenter;
import pl.ismop.web.client.widgets.realtime.side.RealTimeSidePanelPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;

@Events(startPresenter = RootPresenter.class, historyOnStart = true)
public interface MainEventBus extends EventBusWithLookup {
	@InitHistory
	@Event(handlers = RootPresenter.class, historyConverter = MenuHistoryConverter.class,
			deactivate = {VerticalSliceWizardPresenter.class, HorizontalSliceWizardPresenter.class})
	void monitoringPanel();

	@Event(handlers = RootPresenter.class, historyConverter = MenuHistoryConverter.class)
	void analysisPanel();

	@Event(handlers = RootPresenter.class, historyConverter = MenuHistoryConverter.class)
	void realTimePanel();

	@Event(handlers = ErrorPresenter.class)
	void showError(ErrorDetails errorDetails);

	@Event(handlers = WeatherStationPresenter.class)
	void showWeatherPanel();

	@Event(handlers = FibrePresenter.class)
	void showFibrePanel(Levee levee);

	@Event(handlers = WaterHeightPresenter.class)
	void showWaterHightPanel(Levee selectedLevee);

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

	@Event(handlers = {LeveeNavigatorPresenter.class, HorizontalSliceWizardPresenter.class,
			VerticalSliceWizardPresenter.class})
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

	@Event(handlers = {AnalysisSidePanelPresenter.class, ChartPresenter.class,
			HorizontalSlicePresenter.class, VerticalSlicePresenter.class})
	void dateChanged(Date selectedDate);

	@Event(handlers = {AnalysisSidePanelPresenter.class, ChartPresenter.class})
	void refresh();

	@Event(handlers = ThreatLevelsPresenter.class)
	void threatLevelsChanged(String msg);

	@Event(handlers = { DummyPresenter.class, ComparisonPresenter.class })
	void experimentChanged(Experiment selectedExperiment);

	/**
	 * Add map feature into minimap.
	 * To remove feature from the minimap use {@link #rm(MapFeature)}.
	 *
	 * @param mapFeature Map feature te be added.
     */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void add(MapFeature mapFeature);

	/**
	 * Remove map feature from minimap.
	 * To add feature from the minimap use {@link #add(MapFeature)}.
	 *
	 * @param mapFeature Map feature te be removed.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void rm(MapFeature mapFeature);

	/**
	 * Select map feature on minimap.
	 * To unselect map feature use {@link #unselect(MapFeature)}.
	 *
	 * @param mapFeature MapFeature to be selected.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void select(MapFeature mapFeature);

	/**
	 * Unselect device. To select device use {@link #select(MapFeature)}.
	 *
	 * @param mapFeature MapFeature to be unselected.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void unselect(MapFeature mapFeature);

	/**
	 * Highlight map feature on minimap.
	 * To unhighlight map feature use {@link #unhighlight(MapFeature)}.
	 *
	 * @param mapFeature MapFeature to be highlighted.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void highlight(MapFeature mapFeature);

	/**
	 * Unhighlight map feature on minimap.
	 * To highlight map feature use {@link #highlight(MapFeature)}.
	 *
	 * @param mapFeature MapFeature to be unhighlighted.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void unhighlight(MapFeature mapFeature);

	/**
	 * Remove all selections and shows from the minimap.
	 */
	@Event(handlers = AnalysisSidePanelPresenter.class )
	void clearMinimap();

	@Event(handlers = ChartWizardPresenter.class)
	void timelineSelectionChanged();

	@Event(handlers = HorizontalSliceWizardPresenter.class,
			activate = HorizontalSliceWizardPresenter.class,
			deactivate = { LeveeNavigatorPresenter.class, VerticalSliceWizardPresenter.class })
	void showHorizontalCrosssectionWizard(Experiment experiment);

	@Event(handlers = HorizontalSliceWizardPresenter.class,
			activate = HorizontalSliceWizardPresenter.class,
			deactivate = { LeveeNavigatorPresenter.class, VerticalSliceWizardPresenter.class })
	void showHorizontalCrosssectionWizardWithConfig(
			HorizontalCrosssectionConfiguration configuration);

	@Event(handlers = HorizontalSlicePresenter.class)
	void updateHorizontalSliceConfiguration(HorizontalCrosssectionConfiguration configuration);

	@Event(activate = LeveeNavigatorPresenter.class,
			deactivate = HorizontalSliceWizardPresenter.class)
	void horizontalCrosssectionWizardHidden();

	@Event(handlers = VerticalSliceWizardPresenter.class,
			activate = VerticalSliceWizardPresenter.class,
			deactivate = { LeveeNavigatorPresenter.class, HorizontalSliceWizardPresenter.class })
	void showVerticalCrosssectionWizard(Experiment experiment);

	@Event(handlers = VerticalSliceWizardPresenter.class,
			activate = VerticalSliceWizardPresenter.class,
			deactivate = { LeveeNavigatorPresenter.class, HorizontalSliceWizardPresenter.class })
	void showVerticalCrosssectionWizardWithConfig(VerticalCrosssectionConfiguration configuration);

	@Event(handlers = VerticalSlicePresenter.class)
	void updateVerticalSliceConfiguration(VerticalCrosssectionConfiguration configuration);

	@Event(activate = LeveeNavigatorPresenter.class,
			deactivate = VerticalSliceWizardPresenter.class)
	void verticalCrosssectionWizardHidden();

	@Event(handlers = ErrorPresenter.class)
	void showSimpleError(String errorDetails);

	@Event(handlers = RealTimePanelPresenter.class)
	void refreshRealTimePanel();

	@Event(handlers = RealTimeSidePanelPresenter.class)
	void realDataContentLoaded();

	@Event(handlers = { VerticalSlicePresenter.class, HorizontalSlicePresenter.class })
	void gradientExtended(String gradientId);

	@Event(handlers = { RealTimeSidePanelPresenter.class })
	void addDeviceToRealtimeMap(Device device);

	@Event(handlers = { RealTimeSidePanelPresenter.class })
	void selectDeviceOnRealtimeMap(Device device, boolean select);

	@Event(handlers = { RealTimeSidePanelPresenter.class })
	void removeProfileFromRealtimeMap(Profile profile);

	@Event(handlers = { RealTimeSidePanelPresenter.class })
	void addProfileFromRealtimeMap(Profile profile);
}