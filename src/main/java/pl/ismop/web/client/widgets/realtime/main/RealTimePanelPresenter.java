package pl.ismop.web.client.widgets.realtime.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.i18n.client.NumberFormat;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.util.TimelineZoomDataCallbackHelper;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSlicePresenter;
import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSlicePresenter;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter.DeviceSelectHandler;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.realtime.main.IRealTimePanelView.IRealTimePanelPresenter;

@Presenter(view = RealTimePanelView.class, multiple = true)
public class RealTimePanelPresenter extends BasePresenter<IRealTimePanelView, MainEventBus>
		implements IRealTimePanelPresenter {
	
	private DapController dapController;
	
	private Map<String, Device> weatherDevices;
	
	private List<Parameter> weatherParameters;
	
	private List<Measurement> weatherMeasurements;

	private String currentWeatherDeviceId;

	private IsmopConverter ismopConverter;
	
	private List<String> chartDeviceCustomIds;
	
	private List<Parameter> chartParameters;
	
	private List<Measurement> chartMeasurements;
	
	private List<Device> chartDevices;
	
	private ChartPresenter chartPresenter;
	
	private Parameter waterLevelParameter;
	
	private List<Measurement> waterLevelMeasurements;
	
	private List<Profile> verticalSliceProfiles;
	
	private Profile currentVerticalSliceProfile;
	
	private List<Parameter> verticalSliceParameters;
	
	private List<Device> verticalSliceDevices;

	private VerticalSlicePresenter verticalSlicePresenter;

	private String currentVerticalSliceParameterName;

	private VerticalCrosssectionConfiguration currentVerticalConfiguration;

	private HorizontalCrosssectionConfiguration currentHorizontalConfiguration;

	private List<Section> horizontalSliceSections;

	private ArrayList<Profile> horizontalSliceProfiles;

	private List<Device> horizontalSliceDevices;

	private List<Parameter> horizontalSliceParameters;

	private String currentHorizontalSliceParameterName;

	private HorizontalSlicePresenter horizontalSlicePresenter;
	
	@Inject
	public RealTimePanelPresenter(DapController dapController, IsmopConverter ismopConverter) {
		this.dapController = dapController;
		this.ismopConverter = ismopConverter;
		chartDeviceCustomIds = Arrays.asList("UT6", "UT18", "UT29", "UT5", "UT17", "UT28");
	}
	
	public void init() {
		onRefreshRealTimePanel();
	}
	
	public void onRefreshRealTimePanel() {
		view.showLoadingIndicator(true);
		
		ListenableFuture<Void> weatherFuture = updateWeather();
		ListenableFuture<Void> chartFuture = updateChart();
		ListenableFuture<Void> waterLevelFuture = updateWaterLevel();
		ListenableFuture<Void> verticalSliceFuture = updateVerticalSlice();
		ListenableFuture<Void> horizontalSliceFuture = updateHorizontalSlice();
		Futures.whenAllComplete(weatherFuture, chartFuture, waterLevelFuture, verticalSliceFuture,
				horizontalSliceFuture)
				.call(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						view.showLoadingIndicator(false);
						eventBus.realDataContentLoaded();
						
						return null;
					}
				});
	}

	@Override
	public void onWeatherSourceChange() {
		Iterator<String> weatherDeviceIdIterator = weatherDevices.keySet().iterator();
		String weatherDeviceId = weatherDeviceIdIterator.next();
		
		while (weatherDeviceIdIterator.hasNext()
				&& weatherDeviceId.equals(currentWeatherDeviceId)) {
			weatherDeviceId = weatherDeviceIdIterator.next();
		}
		
		currentWeatherDeviceId = weatherDeviceId;
		renderWeatherData();
	}

	@Override
	public void onVerticalSliceParameterChange() {
		//assuming there are only two different parameter names
		for (Parameter parameter : verticalSliceParameters) {
			if (!currentVerticalSliceParameterName.equals(parameter.getMeasurementTypeName())) {
				currentVerticalSliceParameterName = parameter.getMeasurementTypeName();
				
				break;
			}
		}
		
		currentVerticalConfiguration.setPickedParameterMeasurementName(
				currentVerticalSliceParameterName);
		verticalSlicePresenter.setConfiguration(currentVerticalConfiguration);
		verticalSlicePresenter.onDateChanged(new Date());
		view.setVerticalSliceHeading(
				currentVerticalConfiguration.getPickedParameterMeasurementName());
	}

	@Override
	public void onHorizontalSliceParameterChange() {
		//assuming there are only two different parameter names
		for (Parameter parameter : horizontalSliceParameters) {
			if (!currentHorizontalSliceParameterName.equals(parameter.getMeasurementTypeName())) {
				currentHorizontalSliceParameterName = parameter.getMeasurementTypeName();
				
				break;
			}
		}
		
		currentHorizontalConfiguration.setPickedParameterMeasurementName(
				currentHorizontalSliceParameterName);
		horizontalSlicePresenter.setConfiguration(currentHorizontalConfiguration);
		horizontalSlicePresenter.onDateChanged(new Date());
		view.setHorizontalSliceHeading(
				currentHorizontalConfiguration.getPickedParameterMeasurementName());
	}

	public void cleanUp() {
		//this fix is needed to redraw chart and prevent an ugly error from happening
		if (chartPresenter != null) {
			chartPresenter.reset();
		}
	}

	private ListenableFuture<Void> updateWeather() {
		SettableFuture<Void> result = SettableFuture.create();
		ListenableFuture<List<String>> deviceIdsFuture = Futures.transform(
				dapController.getDevicesForType("weather_station"), devices -> {
					weatherDevices = Maps.uniqueIndex(devices, Device::getId);
					
					return Lists.transform(devices, Device::getId);
				});
		ListenableFuture<List<Parameter>> parametersFuture = Futures.transformAsync(deviceIdsFuture,
				deviceIds -> dapController.getParameters(deviceIds));
		ListenableFuture<List<Context>> contextsFuture = dapController.getContext("measurements");
		ListenableFuture<List<List<? extends Object>>> contextsAndParameterIds =
				Futures.allAsList(contextsFuture, parametersFuture);
		ListenableFuture<List<Timeline>> timelinesFuture =
				Futures.transformAsync(contextsAndParameterIds,
				(List<List<? extends Object>> resultList) -> {
					@SuppressWarnings("unchecked")
					List<Context> contexts = (List<Context>) resultList.get(0);
					@SuppressWarnings("unchecked")
					List<Parameter> parameters = (List<Parameter>) resultList.get(1);
					weatherParameters = parameters;
					
					if (contexts.size() > 0 && parameters.size() > 0) {
						return dapController.getTimelinesForParameterIds(
								contexts.get(0).getId(), Lists.transform(parameters,
										parameter -> parameter.getId()));
					} else {
						SettableFuture<List<Timeline>> resultFuture = SettableFuture.create();
						resultFuture.set(new ArrayList<>());
						
						return resultFuture;
					}
				});
		ListenableFuture<List<Measurement>> measurementsFuture =
				Futures.transformAsync(timelinesFuture,
					timelines -> dapController.getLastMeasurementsWith24HourMod(
						Lists.transform(timelines, timeline -> timeline.getId()), new Date()));
		Futures.addCallback(measurementsFuture, new FutureCallback<List<Measurement>>() {
			@Override
			public void onSuccess(List<Measurement> measurements) {
				weatherMeasurements = measurements;
				renderWeatherData();
				result.set(null);
			}

			@Override
			public void onFailure(Throwable t) {
				eventBus.showError(new ErrorDetails(t.getMessage()));
				result.set(null);
			}
		});
		
		return result;
	}

	private void renderWeatherData() {
		if (currentWeatherDeviceId == null) {
			List<String> sortedWeatherDeviceIds = new ArrayList<>(weatherDevices.keySet());
			Collections.sort(sortedWeatherDeviceIds,
					(d1, d2) -> weatherDevices.get(d1).getCustomId()
							.compareTo(weatherDevices.get(d2).getCustomId()));
			currentWeatherDeviceId = sortedWeatherDeviceIds.get(0);
		}
		
		view.setWeatherSectionTitle(weatherDevices.get(currentWeatherDeviceId).getCustomId());
		
		ListMultimap<Parameter, Measurement> readings = ArrayListMultimap.create();
		
		for (Parameter parameter : weatherParameters) {
			for (Measurement measurement : weatherMeasurements) {
				if (parameter.getTimelineIds().contains(measurement.getTimelineId())) {
					readings.put(parameter, measurement);
				}
			}
		}
		
		List<Parameter> sortedParameters = new ArrayList<>(readings.keys());
		
		for (Iterator<Parameter> i = sortedParameters.iterator(); i.hasNext();) {
			if (!i.next().getDeviceId().equals(currentWeatherDeviceId)) {
				i.remove();
			}
		}
		
		Collections.sort(sortedParameters, (p1, p2) -> p1.getMeasurementTypeName()
				.compareTo(p2.getMeasurementTypeName()));
		
		for (int i = 0; i < sortedParameters.size(); i++) {
			view.setWeatherParameter(i, sortedParameters.get(i).getMeasurementTypeName(),
					"" + NumberFormat.getFormat("0.00").format(
							readings.get(sortedParameters.get(i)).get(0).getValue())
					+ " " + sortedParameters.get(i).getMeasurementTypeUnit(),
					ismopConverter.formatForDisplay(readings.get(sortedParameters.get(i)).get(0)
							.getTimestamp()));
			
		}
	}

	private ListenableFuture<Void> updateChart() {
		SettableFuture<Void> result = SettableFuture.create();
		
		ListenableFuture<List<String>> deviceIdsFuture = Futures.transform(
				dapController.getDevicesWithCustomIds(chartDeviceCustomIds),
					devices -> {
						chartDevices = devices;
						
						return Lists.transform(devices, Device::getId);
					});
		ListenableFuture<List<Parameter>> parametersFuture = Futures.transformAsync(deviceIdsFuture,
				deviceIds -> dapController.getParameters(deviceIds));
		ListenableFuture<List<Context>> contextsFuture = dapController.getContext("measurements");
		ListenableFuture<List<List<? extends Object>>> contextsAndParameterIds =
				Futures.allAsList(contextsFuture, parametersFuture);
		ListenableFuture<List<Timeline>> timelinesFuture =
				Futures.transformAsync(contextsAndParameterIds,
				(List<List<? extends Object>> resultList) -> {
					@SuppressWarnings("unchecked")
					List<Context> contexts = (List<Context>) resultList.get(0);
					@SuppressWarnings("unchecked")
					List<Parameter> parameters = (List<Parameter>) resultList.get(1);
					chartParameters = parameters;
					
					if (contexts.size() > 0 && parameters.size() > 0) {
						return dapController.getTimelinesForParameterIds(
								contexts.get(0).getId(), Lists.transform(parameters,
										parameter -> parameter.getId()));
					} else {
						SettableFuture<List<Timeline>> resultFuture = SettableFuture.create();
						resultFuture.set(new ArrayList<>());
						
						return resultFuture;
					}
				});
		
		Date currentDate = new Date();
		Date earlierBy24Hours = new Date(currentDate.getTime() - 24 * 60 * 60 * 1000);
		ListenableFuture<List<Measurement>> measurementsFuture =
				Futures.transformAsync(timelinesFuture,
					timelines -> dapController.getMeasurementsWithQuantityAndTime(
						Lists.transform(timelines, timeline -> timeline.getId()), earlierBy24Hours,
						currentDate, 1000));
		Futures.addCallback(measurementsFuture, new FutureCallback<List<Measurement>>() {
			@Override
			public void onSuccess(List<Measurement> measurements) {
				chartMeasurements = measurements;
				renderChartData();
				result.set(null);
			}

			@Override
			public void onFailure(Throwable t) {
				eventBus.showError(new ErrorDetails(t.getMessage()));
				result.set(null);
			}
		});
		
		return result;
	}

	private void renderChartData() {
		if (chartPresenter == null) {
			chartPresenter = eventBus.addHandler(ChartPresenter.class);
			chartPresenter.setDeviceSelectHandler(new DeviceSelectHandler() {
				@Override
				public void unselect(ChartSeries series) {
					Optional<Device> device = Iterables.tryFind(chartDevices, d -> d.getId()
							.equals(series.getDeviceId()));
					
					if (device.isPresent()) {
						eventBus.selectDeviceOnRealtimeMap(device.get(), false);
					}
				}
				
				@Override
				public void select(ChartSeries series) {
					Optional<Device> device = Iterables.tryFind(chartDevices, d -> d.getId()
							.equals(series.getDeviceId()));
					
					if (device.isPresent()) {
						eventBus.selectDeviceOnRealtimeMap(device.get(), true);
					}
				}
			});
			chartPresenter.setHeight(220);
			view.setChartView(chartPresenter.getView());
			chartPresenter.initChart();
			chartPresenter.setZoomDataCallback(new TimelineZoomDataCallbackHelper(dapController,
					eventBus, chartPresenter));
		}
		
		chartPresenter.addChartSeriesList(createChartSeries());
		
		for (Device chartDevice : chartDevices) {
			eventBus.addDeviceToRealtimeMap(chartDevice);
		}
	}

	private List<ChartSeries> createChartSeries() {
		List<ChartSeries> result = new ArrayList<>();
		
		for (Parameter parameter : chartParameters) {
			ChartSeries series = new ChartSeries();
			Device device = Iterables.find(chartDevices, d -> d.getParameterIds()
					.contains(parameter.getId()));
			series.setName(device.getCustomId() + " (" + parameter.getMeasurementTypeName() + ")");
			series.setDeviceId(parameter.getDeviceId());
			series.setParameterId(parameter.getId());
			series.setLabel(parameter.getMeasurementTypeName());
			series.setUnit(parameter.getMeasurementTypeUnit());
			series.setTimelineId(parameter.getTimelineIds().get(0));
			
			List<Measurement> parameterMeasurements = Lists.newArrayList(
					Iterables.filter(chartMeasurements,
						measurement -> measurement.getTimelineId().equals(
								parameter.getTimelineIds().get(0))));
			
			Number[][] values = new Number[parameterMeasurements.size()][2];
			int index = 0;
			
			for(Measurement measurement : parameterMeasurements) {
				values[index][0] = measurement.getTimestamp().getTime();
				values[index][1] = measurement.getValue();
				index++;
			}
			
			series.setValues(values);
			result.add(series);
		}
		
		return result;
	}

	private ListenableFuture<Void> updateWaterLevel() {
		SettableFuture<Void> result = SettableFuture.create();
		ListenableFuture<List<String>> deviceIdsFuture = Futures.transform(
				dapController.getDevicesForType("pump"),
					devices -> Lists.transform(devices, Device::getId)
				);
		ListenableFuture<List<Parameter>> parametersFuture = Futures.transformAsync(deviceIdsFuture,
				deviceIds -> dapController.getParameters(deviceIds));
		ListenableFuture<List<Context>> contextsFuture = dapController.getContext("measurements");
		ListenableFuture<List<List<? extends Object>>> contextsAndParameterIds =
				Futures.allAsList(contextsFuture, parametersFuture);
		ListenableFuture<List<Timeline>> timelinesFuture =
				Futures.transformAsync(contextsAndParameterIds,
				(List<List<? extends Object>> resultList) -> {
					@SuppressWarnings("unchecked")
					List<Context> contexts = (List<Context>) resultList.get(0);
					@SuppressWarnings("unchecked")
					List<Parameter> parameters = (List<Parameter>) resultList.get(1);
					waterLevelParameter = Iterables.find(parameters,
							p -> p.getParameterName()
								.equals("Monitorowanie wysokości fali - Level1_PV"));
					
					if (contexts.size() > 0 && parameters.size() > 0) {
						return dapController.getTimelinesForParameterIds(
								contexts.get(0).getId(),
									Arrays.asList(waterLevelParameter.getId()));
					} else {
						SettableFuture<List<Timeline>> resultFuture = SettableFuture.create();
						resultFuture.set(new ArrayList<>());
						
						return resultFuture;
					}
				});
		ListenableFuture<List<Measurement>> measurementsFuture =
				Futures.transformAsync(timelinesFuture,
					timelines -> dapController.getLastMeasurementsWith24HourMod(
						Lists.transform(timelines, timeline -> timeline.getId()), new Date()));
		Futures.addCallback(measurementsFuture, new FutureCallback<List<Measurement>>() {
			@Override
			public void onSuccess(List<Measurement> measurements) {
				waterLevelMeasurements = measurements;
				renderWaterLevelData();
				result.set(null);
			}

			@Override
			public void onFailure(Throwable t) {
				eventBus.showError(new ErrorDetails(t.getMessage()));
				result.set(null);
			}

		});
		
		
		return result;
	}
	
	private void renderWaterLevelData() {
		if (waterLevelParameter != null && waterLevelMeasurements.size() > 0) {
			view.setWaterLevelValue("" + NumberFormat.getFormat("0.00")
				.format(waterLevelMeasurements.get(0).getValue()) + " "
					+ waterLevelParameter.getMeasurementTypeUnit());
			view.setWaterLevelDate(ismopConverter.formatForDisplay(
					waterLevelMeasurements.get(0).getTimestamp()));
		} else {
			view.setEmptyWaterLevelValues();
		}
	}

	private ListenableFuture<Void> updateVerticalSlice() {
		SettableFuture<Void> result = SettableFuture.create();
		ListenableFuture<List<Section>> sectionsFuture = dapController.getSections();
		ListenableFuture<List<String>> sectionIdsFuture = Futures.transform(sectionsFuture,
				sections -> Lists.transform(sections, Section::getId));
		ListenableFuture<List<Profile>> profilesFuture = Futures.transformAsync(sectionIdsFuture,
				sectionIds -> dapController.getProfiles(sectionIds));
		ListenableFuture<List<String>> profileIdsFuture = Futures.transform(profilesFuture,
				profiles -> {
					verticalSliceProfiles = Lists.newArrayList(Iterables.filter(profiles,
							profile -> profile.getDeviceAggregationIds() != null &&
								profile.getDeviceAggregationIds().size() > 0));
					currentVerticalSliceProfile = getCurrentVerticalSliceProfileAndUpdateMap();
					
					return Lists.newArrayList(currentVerticalSliceProfile.getId());
				});
		ListenableFuture<List<DeviceAggregate>> deviceAggregationsFuture = Futures.transformAsync(
				profileIdsFuture, profileIds -> dapController.getDeviceAggregations(profileIds));
		ListenableFuture<List<String>> deviceAggregateIdsFuture = Futures.transform(
				deviceAggregationsFuture, deviceAggregates -> Lists.transform(
						deviceAggregates, DeviceAggregate::getId));
		ListenableFuture<List<Device>> devicesFuture = Futures.transformAsync(
				deviceAggregateIdsFuture, deviceAggregateIds ->
					dapController.getDevicesRecursivelyForAggregates(deviceAggregateIds));
		ListenableFuture<List<String>> deviceIdsFuture = Futures.transform(devicesFuture,
				devices -> {
					verticalSliceDevices = devices;
					
					return Lists.transform(devices, Device::getId);
				});
		ListenableFuture<List<Parameter>> parametersFuture = Futures.transformAsync(deviceIdsFuture,
				deviceIds -> dapController.getParameters(deviceIds));
		Futures.addCallback(parametersFuture, new FutureCallback<List<Parameter>>() {
			@Override
			public void onSuccess(List<Parameter> parameters) {
				verticalSliceParameters = parameters;
				renderVerticalSliceData();
				result.set(null);
			}

			@Override
			public void onFailure(Throwable t) {
				eventBus.showError(new ErrorDetails(t.getMessage()));
				result.set(null);
			}
		});
		
		return result;
	}

	private Profile getCurrentVerticalSliceProfileAndUpdateMap() {
		Profile result;
		
		if (currentVerticalSliceProfile == null) {
			result = verticalSliceProfiles.get(0);
		} else {
			eventBus.removeProfileFromRealtimeMap(currentVerticalSliceProfile);
			
			if (verticalSliceProfiles.contains(currentVerticalSliceProfile)) {
				result = verticalSliceProfiles.get((verticalSliceProfiles.indexOf(
						currentVerticalSliceProfile) + 1) % verticalSliceProfiles.size());
			} else {
				result = verticalSliceProfiles.get(0);
			}
		}
		
		return result;
	}

	private void renderVerticalSliceData() {
		currentVerticalConfiguration = new VerticalCrosssectionConfiguration();
		currentVerticalConfiguration.setDataSelector("0"); //0 means real values
		currentVerticalConfiguration.setPickedProfile(currentVerticalSliceProfile);
		
		currentVerticalSliceParameterName = getCurrentVerticalSliceParameterName();
		currentVerticalConfiguration.setPickedParameterMeasurementName(
				currentVerticalSliceParameterName);
		view.setVerticalSliceHeading(
				currentVerticalConfiguration.getPickedParameterMeasurementName());
		
		Map<Profile, List<Device>> profileDevicesMap = new HashMap<>();
		profileDevicesMap.put(currentVerticalSliceProfile, Lists.newArrayList(
				Iterables.filter(verticalSliceDevices,
						device -> device.getProfileId()
							.equals(currentVerticalSliceProfile.getId()))));
		currentVerticalConfiguration.setProfileDevicesMap(profileDevicesMap);
		currentVerticalConfiguration.setParameterMap(Maps.uniqueIndex(verticalSliceParameters,
				Parameter::getId));
		
		if (verticalSlicePresenter == null) {
			verticalSlicePresenter = eventBus.addHandler(VerticalSlicePresenter.class);
			view.setVerticalSliceView(verticalSlicePresenter.getView());
		}
		
		verticalSlicePresenter.setConfiguration(currentVerticalConfiguration);
		verticalSlicePresenter.onDateChanged(new Date());
		eventBus.addProfileFromRealtimeMap(currentVerticalSliceProfile);
	}

	private String getCurrentVerticalSliceParameterName() {
		if (currentVerticalSliceParameterName == null) {
			return verticalSliceParameters.get(0).getMeasurementTypeName();
		} else {
			return currentVerticalSliceParameterName;
		}
	}

	private ListenableFuture<Void> updateHorizontalSlice() {
		SettableFuture<Void> result = SettableFuture.create();
		ListenableFuture<List<Section>> sectionsFuture = dapController.getSections();
		ListenableFuture<List<String>> sectionIdsFuture = Futures.transform(sectionsFuture,
				sections -> {
					horizontalSliceSections = sections;
					
					return Lists.transform(sections, Section::getId);
				});
		ListenableFuture<List<Profile>> profilesFuture = Futures.transformAsync(sectionIdsFuture,
				sectionIds -> dapController.getProfiles(sectionIds));
		ListenableFuture<List<String>> profileIdsFuture = Futures.transform(profilesFuture,
				profiles -> {
					//picking only single profiles per section
					horizontalSliceProfiles = new ArrayList<>();
					List<String> sectionIds = new ArrayList<>();
					
					for (Profile profile : profiles) {
						if (profile.getDeviceAggregationIds().size() > 0
								&& !sectionIds.contains(profile.getSectionId())) {
							horizontalSliceProfiles.add(profile);
							sectionIds.add(profile.getSectionId());
						}
					}
					
					return Lists.newArrayList(Iterables.transform(horizontalSliceProfiles,
							Profile::getId));
				});
		ListenableFuture<List<DeviceAggregate>> deviceAggregationsFuture = Futures.transformAsync(
				profileIdsFuture, profileIds -> dapController.getDeviceAggregations(profileIds));
		ListenableFuture<List<String>> deviceAggregateIdsFuture = Futures.transform(
				deviceAggregationsFuture, deviceAggregates -> Lists.transform(
						deviceAggregates, DeviceAggregate::getId));
		ListenableFuture<List<Device>> devicesFuture = Futures.transformAsync(
				deviceAggregateIdsFuture, deviceAggregateIds ->
					dapController.getDevicesRecursivelyForAggregates(deviceAggregateIds));
		ListenableFuture<List<String>> deviceIdsFuture = Futures.transform(devicesFuture,
				devices -> {
					horizontalSliceDevices = devices;
					
					return Lists.transform(devices, Device::getId);
				});
		ListenableFuture<List<Parameter>> parametersFuture = Futures.transformAsync(deviceIdsFuture,
				deviceIds -> dapController.getParameters(deviceIds));
		Futures.addCallback(parametersFuture, new FutureCallback<List<Parameter>>() {
			@Override
			public void onSuccess(List<Parameter> parameters) {
				horizontalSliceParameters = parameters;
				renderHorizontalSliceData();
				result.set(null);
			}

			@Override
			public void onFailure(Throwable t) {
				eventBus.showError(new ErrorDetails(t.getMessage()));
				result.set(null);
			}

		});
		
		return result;
	}
	
	private void renderHorizontalSliceData() {
		currentHorizontalConfiguration = new HorizontalCrosssectionConfiguration();
		currentHorizontalConfiguration.setDataSelector("0"); //0 means real values
		currentHorizontalConfiguration.setSections(Maps.uniqueIndex(horizontalSliceSections,
				Section::getId));
		currentHorizontalSliceParameterName = getCurrentHorizontalSliceParameterName();
		currentHorizontalConfiguration.setPickedParameterMeasurementName(
				currentHorizontalSliceParameterName);
		view.setHorizontalSliceHeading(currentHorizontalConfiguration.getPickedParameterMeasurementName());
		currentHorizontalConfiguration.setParameterMap(Maps.uniqueIndex(horizontalSliceParameters,
				Parameter::getId));
		currentHorizontalConfiguration.setPickedHeights(Maps.toMap(horizontalSliceProfiles,
				profile -> "fakeHeight"));
		
		Map<String, List<Device>> heightDeviceMap = new HashMap<>();
		heightDeviceMap.put("fakeHeight", horizontalSliceDevices);
		currentHorizontalConfiguration.setHeightDevicesmap(heightDeviceMap);
		currentHorizontalConfiguration.setPickedProfiles(Maps.uniqueIndex(horizontalSliceProfiles,
				Profile::getId));
		currentHorizontalConfiguration.setProfileDevicesMap(Multimaps.asMap(Multimaps.index(horizontalSliceDevices,
				device -> currentHorizontalConfiguration.getPickedProfiles().get(device.getProfileId()))));
		
		if (horizontalSlicePresenter == null) {
			horizontalSlicePresenter = eventBus.addHandler(HorizontalSlicePresenter.class);
			view.setHorizontalSliceView(horizontalSlicePresenter.getView());
		}
		
		horizontalSlicePresenter.setConfiguration(currentHorizontalConfiguration);
		horizontalSlicePresenter.onDateChanged(new Date());
	}

	private String getCurrentHorizontalSliceParameterName() {
		if (currentHorizontalSliceParameterName == null) {
			return horizontalSliceParameters.get(0).getMeasurementTypeName();
		} else {
			return currentHorizontalSliceParameterName;
		}
	}
}