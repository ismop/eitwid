package pl.ismop.web.client.widgets.analysis.verticalslice;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.core.client.GWT;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.util.CoordinatesUtil;
import pl.ismop.web.client.util.GradientsUtil;
import pl.ismop.web.client.util.GradientsUtil.Color;
import pl.ismop.web.client.widgets.analysis.verticalslice.IVerticalSliceView.IVerticalSlicePresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = VerticalSliceView.class, multiple = true)
public class VerticalSlicePresenter extends BasePresenter<IVerticalSliceView, MainEventBus>
		implements IVerticalSlicePresenter, IPanelContent<IVerticalSliceView, MainEventBus> {

	public static class Point {

		double x, y, value;

		int r, g, b;

		Device device;

		boolean virtual;

		boolean fakeValue;

		Point(double x, double y, boolean virtual) {
			this.x = x;
			this.y = y;
			this.virtual = virtual;
		}

		Point(double x, double y, boolean virtual, Device device) {
			this(x, y, virtual);
			this.device = device;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + ", value=" + value + ", r=" + r + ", g=" + g
					+ ", b=" + b + "]";
		}
	}

	public static class Borehole {

		double x;

		boolean virtual;

		List<Point> points;

		Borehole(double x, List<Point> points) {
			this.x = x;
			this.points = points;
		}

		Borehole(double x, List<Point> points, boolean virtual) {
			this(x, points);
			this.virtual = virtual;
		}

		@Override
		public String toString() {
			return "Borehole [x=" + x + ", virtual=" + virtual + ", points=" + points + "]";
		}
	}

	private static final double PROFILE_HEIGHT = 4.5;

	private static final double PROFILE_TOP_WIDTH = 4;

	private DapController dapController;

	private VerticalCrosssectionConfiguration configuration;

	private Date currentDate;

	private CoordinatesUtil coordinatesUtil;

	private ISelectionManager selectionManager;

	private GradientsUtil gradientsUtil;

	private double gradientMin, gradientMax;

	private String gradientId;

	@Inject
	public VerticalSlicePresenter(DapController dapController, CoordinatesUtil coordinatesUtil,
			GradientsUtil gradientsUtil) {
		this.dapController = dapController;
		this.coordinatesUtil = coordinatesUtil;
		this.gradientsUtil = gradientsUtil;
	}

	public void onUpdateVerticalSliceConfiguration(
			VerticalCrosssectionConfiguration configuration) {
		if (this.configuration == configuration) {
			refreshView();
		}

		selectChosenProfileOnMinimap();
	}

	public void onDateChanged(Date selectedDate) {
		currentDate = selectedDate;
		refreshView();
	}

	public void setConfiguration(VerticalCrosssectionConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void setSelectedExperiment(Experiment experiment) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectedDate(Date date) {
		currentDate = date;
		refreshView();
	}

	@Override
	public void edit() {
		eventBus.showVerticalCrosssectionWizardWithConfig(configuration);
	}

	@Override
	public void setSelectionManager(ISelectionManager selectionManager) {
		this.selectionManager = selectionManager;
		selectionManager.select(configuration.getPickedProfile());
		selectChosenProfileOnMinimap();
	}

	public void selectChosenProfileOnMinimap() {
		if (selectionManager != null) {
			selectionManager.clear();
			selectionManager.select(configuration.getPickedProfile());
		}
	}

	@Override
	public void destroy() {

	}

	public void onGradientExtended(String gradientId) {
		if (gradientId.equals(this.gradientId)
				&& gradientsUtil.isExtended(gradientId, gradientMin, gradientMax)) {
			refreshView();
		}
	}

	private void refreshView() {
		if (!view.canRender()) {
			eventBus.showSimpleError(view.cannotRenderMessage());

			return;
		}

		view.showLoadingState(true);
		Profile pickedProfile = configuration.getPickedProfile();

		//grouping devices according to their aggregates
		Map<String, List<Device>> devicesByAggragateId = configuration.getProfileDevicesMap()
				.get(pickedProfile).stream()
					.collect(Collectors.groupingBy(Device::getDeviceAggregationId));

		//transforming device aggregate groups into Point lists
		List<Borehole> boreholes = devicesByAggragateId.values().stream()
				.filter(devices -> devices.size() > 0)
				.map(devices -> {
					List<Point> points = devices.stream()
							.map(device -> {
								List<List<Double>> coords = coordinatesUtil.projectCoordinates(
										Arrays.asList(device.getPlacement().getCoordinates()));

								//x becomes new x, z becomes new y in the profile plane
								return new Point(coords.get(0).get(0),
										device.getPlacement().getCoordinates().get(2), false,
										device);
							}).collect(Collectors.toList());

					return new Borehole(points.get(0).x, points);
				}).collect(Collectors.toList());

		//adding profile end points to point lists
		List<List<Double>> profileEndPoints = coordinatesUtil.projectCoordinates(
				pickedProfile.getShape().getCoordinates());
		profileEndPoints.forEach(profileEndPoint -> {
			boreholes.add(
					new Borehole(
							profileEndPoint.get(0),
							Arrays.asList(new Point(profileEndPoint.get(0),
									pickedProfile.getBaseHeight(), true)),
							true)
					);
		});
		normalizeCoordinates(boreholes, pickedProfile.getBaseHeight());

		//adding boreholes corresponding to profile's top end points, each borehole has a couple of
		//intermediary points equal to the maximam number of points in non-virtual boreholes minus 1
		Optional<Integer> numberOfIntermediariesOptional = boreholes.stream()
				.map(borehole -> borehole.points.size())
				.max(Comparator.naturalOrder());
		int numberOfIntermediaries = numberOfIntermediariesOptional.isPresent()
				? numberOfIntermediariesOptional.get() - 1
				: 0;
		Optional<Double> profileWidth = boreholes.stream()
				.flatMap(borehole -> borehole.points.stream())
				.map(point -> point.x)
				.max(Comparator.naturalOrder());
		double topLeftPoint = profileWidth.get() / 2 - PROFILE_TOP_WIDTH / 2;
		double topRightPoint = profileWidth.get() / 2 + PROFILE_TOP_WIDTH / 2;
		Borehole leftVirtualBorehole = new Borehole(topLeftPoint, new ArrayList<>(), true);
		leftVirtualBorehole.points.addAll(Arrays.asList(
				new Point(topLeftPoint, PROFILE_HEIGHT, true),
				new Point(topLeftPoint, 0.0, true)));
		Borehole rightVirtualBorehole = new Borehole(topRightPoint, new ArrayList<>(), true);
		rightVirtualBorehole.points.addAll(Arrays.asList(
				new Point(topRightPoint, PROFILE_HEIGHT, true),
				new Point(topRightPoint, 0.0, true)));
		boreholes.add(leftVirtualBorehole);
		boreholes.add(rightVirtualBorehole);
		IntStream.rangeClosed(1, numberOfIntermediaries).forEach(intermediaryIndex -> {
			double height = (PROFILE_HEIGHT / (numberOfIntermediaries + 1))
					* intermediaryIndex;
			leftVirtualBorehole.points.add(new Point(leftVirtualBorehole.x, height, true));
			rightVirtualBorehole.points.add(new Point(rightVirtualBorehole.x, height, true));
		});

		//sorting boreholes and points
		boreholes.forEach(borehole -> borehole.points.sort(
				(point1, point2) -> Double.compare(point1.y, point2.y)));
		boreholes.sort((borehole1, borehole2) -> Double.compare(borehole1.x, borehole2.x));

		//adding boundary points to non-virtual boreholes
		Map<Boolean, List<Borehole>> boreholesByVirtual = boreholes.stream().
				collect(Collectors.partitioningBy(borehole -> borehole.virtual));
		IntStream.range(0, boreholesByVirtual.get(true).size() - 1).forEach(index -> {
			Borehole leftBorehole = boreholesByVirtual.get(true).get(index);
			Borehole rightBorehole = boreholesByVirtual.get(true).get(index + 1);

			for (Borehole nonVirtualBorehole : boreholesByVirtual.get(false)) {
				if (nonVirtualBorehole.x > leftBorehole.x
						&& nonVirtualBorehole.x < rightBorehole.x) {
					//adding bottom point
					nonVirtualBorehole.points.add(0, new Point(nonVirtualBorehole.x, 0.0, true));
					//adding top point
					double x1 = leftBorehole.x;
					double y1 = leftBorehole.points.get(leftBorehole.points.size() - 1).y;
					double x2 = rightBorehole.x;
					double y2 = rightBorehole.points.get(rightBorehole.points.size() - 1).y;
					double x3 = nonVirtualBorehole.x;
					nonVirtualBorehole.points.add(new Point(
							nonVirtualBorehole.x,
							((y2 - y1) / (x2 - x1)) * (x3 - x1) + y1, true));
				}
			}
		});

		//collecting parameter ids to retrieve measurements
		List<String> parameterIds = collectParameterIds();
		Parameter parameter = parameterIds.size() > 0
				? configuration.getParameterMap().get(parameterIds.get(0))
						: null;
		String parameterUnit = parameter == null ? "noen": parameter.getMeasurementTypeUnit();
		ListenableFuture<Map<Device, Measurement>> measurementsFuture = retrieveMeasurements();

		if (configuration.getDataSelector().equals("0")
				&& parameter.getMeasurementTypeName().equals("Temperatura")) {
			//real data is used for the temperature parameter so the external profile nodes can be
			//assigned temperature values from the weather station
			ListenableFuture<Optional<Measurement>> temperatureFuture = retrieveTemperatureReading();
			ListenableFuture<List<Object>> measurementsWithTemperatureFuture =
					Futures.allAsList(measurementsFuture, temperatureFuture);
			Futures.addCallback(measurementsWithTemperatureFuture,
					new FutureCallback<List<Object>>() {
						@Override
						public void onSuccess(List<Object> result) {
							Map<Device, Measurement> deviceMeasurementMap =
									(Map<Device, Measurement>) result.get(0);
							Optional<Measurement> temperature =
									(Optional<Measurement>) result.get(1);
							String gradientId = "analysis:" + parameterUnit;

							if (temperature.isPresent()) {
								gradientsUtil.updateValues(gradientId,
										temperature.get().getValue());
							}

							setPointValuesAndColors(deviceMeasurementMap, boreholes, parameterUnit,
									gradientId, temperature);
							view.showLoadingState(false);
							view.clear();
							view.init();

							boolean leftBank = false;

							if (configuration.getPickedProfile().getShape()
									.getCoordinates().get(0).get(0) > 19.676851838778) {
								leftBank = true;
							}

							view.drawCrosssection(parameterUnit, leftBank, boreholes,
									createLegend(gradientId));
						}

						@Override
						public void onFailure(Throwable t) {
							view.showLoadingState(false);
							eventBus.showError(new ErrorDetails(t.getMessage()));
						}
			});
		} else {
			//all nodes are assigned values according to the retrieved measurements
			Futures.addCallback(measurementsFuture, new FutureCallback<Map<Device, Measurement>>() {
				@Override
				public void onSuccess(Map<Device, Measurement> deviceMeasurementMap) {
					String gradientId = "analysis:" + parameterUnit;
					setPointValuesAndColors(deviceMeasurementMap, boreholes, parameterUnit,
							gradientId, Optional.empty());
					view.showLoadingState(false);
					view.clear();
					view.init();

					boolean leftBank = false;

					if (configuration.getPickedProfile().getShape()
							.getCoordinates().get(0).get(0) > 19.676851838778) {
						leftBank = true;
					}

					view.drawCrosssection(parameterUnit, leftBank, boreholes,
							createLegend(gradientId));
					GWT.log("All boreholes after processing: " + boreholes);
				}

				@Override
				public void onFailure(Throwable t) {
					view.showLoadingState(false);
					eventBus.showError(new ErrorDetails(t.getMessage()));
				}
			});
		}
	}

	private ListenableFuture<Optional<Measurement>> retrieveTemperatureReading() {
		ListenableFuture<List<Device>> weatherDevicesFuture =
				dapController.getDevicesForType("weather_station");
		ListenableFuture<Device> deviceFuture = Futures.transform(weatherDevicesFuture,
				weatherDevices -> {
					if (weatherDevices.size() > 0) {
						return weatherDevices.get(0);
					} else {
						return null;
					}
		});
		ListenableFuture<List<Parameter>> temperatureParametersFuture = Futures.transformAsync(
				deviceFuture, device -> dapController.getParameters(Arrays.asList(device.getId())));
		ListenableFuture<Parameter> temperatureParameterFuture = Futures.transform(
				temperatureParametersFuture, parameters -> {
					Optional<Parameter> temperatureParameter = parameters.stream()
						.filter(parameter -> parameter.getMeasurementTypeName()
								.equals("Temperatura"))
						.findFirst();

					return temperatureParameter.get();
				});
		ListenableFuture<List<Context>> contextsFuture = dapController.getContext("measurements");
		ListenableFuture<List<Object>> contextsAndParameterFuture = Futures.allAsList(
				contextsFuture, temperatureParameterFuture);
		ListenableFuture<List<Timeline>> timelinesFuture = Futures.transformAsync(
				contextsAndParameterFuture, results -> {
					List<Context> contexts = (List<Context>) results.get(0);
					Parameter temperatureParameter = (Parameter) results.get(1);

					return dapController.getTimelinesForParameterIds(
							contexts.get(0).getId(), Arrays.asList(temperatureParameter.getId()));
				});
		ListenableFuture<List<Measurement>> measurementsFuture = Futures.transformAsync(
				timelinesFuture, timelines -> {
					Date queryDate = configuration.getDataSelector().equals("0")
							? currentDate
							:	new Date(currentDate.getTime()
									- configuration.getExperiment().getStart().getTime());

					return dapController.getLastMeasurementsWith24HourMod(
							Arrays.asList(timelines.get(0).getId()),
							queryDate);
				});

		return Futures.transform(measurementsFuture,
				measurements -> Optional.ofNullable(measurements.get(0)));
	}

	private ListenableFuture<Map<Device, Measurement>> retrieveMeasurements() {
		List<String> parameterIds = collectParameterIds();

		if (parameterIds.size() == 0) {
			SettableFuture<Map<Device, Measurement>> result = SettableFuture.create();
			result.set(new HashMap<>());

			return result;
		}

		Map<String, String> parameterIdDeviceIdMap = new HashMap<>();

		for (Device device : configuration.getProfileDevicesMap().get(
				configuration.getPickedProfile())) {
			List<String> commonParameterIds = new ArrayList<>(device.getParameterIds());
			commonParameterIds.retainAll(parameterIds);

			if (commonParameterIds.size() == 1) {
				parameterIdDeviceIdMap.put(commonParameterIds.get(0), device.getId());
			}
		}

		String context = configuration.getDataSelector().equals("0") ? "measurements" : "scenarios";
		ListenableFuture<List<Context>> contextFuture = dapController.getContext(context);
		ListenableFuture<List<Timeline>> timelinesFuture = Futures.transformAsync(contextFuture,
				contextList -> {
					if (contextList.size() > 0) {
						return dapController.getTimelinesForParameterIds(
								contextList.get(0).getId(), parameterIds);
					}

					SettableFuture<List<Timeline>> emptyFuture = SettableFuture.create();
					emptyFuture.set(new ArrayList<>());

					return emptyFuture;
				});
		ListenableFuture<Map<String, Timeline>> deviceIdTimelineMapFuture =
				Futures.transform(timelinesFuture, timelines -> timelines.stream()
						.filter(timeline -> configuration.getDataSelector().equals("0")
								|| !configuration.getDataSelector().equals("0")
								&& timeline.getScenarioId().equals(configuration.getDataSelector()))
						.collect(Collectors.toMap(
								timeline -> parameterIdDeviceIdMap.get(timeline.getParameterId()),
								Function.identity())));
		ListenableFuture<Map<Device, Measurement>> deviceIdMeasurementMapFuture =
				Futures.transformAsync(deviceIdTimelineMapFuture, deviceIdTimelineMap -> {
					List<String> timelineIds = deviceIdTimelineMap.values().stream()
							.map(Timeline::getId)
							.collect(Collectors.toList());
					Date queryDate = configuration.getDataSelector().equals("0")
							? currentDate
							:	new Date(currentDate.getTime()
									- configuration.getExperiment().getStart().getTime());
					ListenableFuture<List<Measurement>> measurementsFuture =
							dapController.getLastMeasurementsWith24HourMod(timelineIds, queryDate);

					return Futures.transform(measurementsFuture, measurements -> {
						Map<Device, Measurement> result = new HashMap<>();
						Map<String, String> timelineIdDeviceIdMap = deviceIdTimelineMap.keySet()
								.stream().collect(Collectors.toMap(
										deviceId -> deviceIdTimelineMap.get(deviceId).getId(),
										Function.identity()));
						measurements.forEach(measurement -> {
							if (timelineIdDeviceIdMap.keySet().contains(
									measurement.getTimelineId())) {
								String deviceId = timelineIdDeviceIdMap.get(
										measurement.getTimelineId());
								Optional<Device> foundDevice = configuration.getProfileDevicesMap()
										.get(configuration.getPickedProfile())
										.stream()
										.filter(device -> device.getId().equals(deviceId))
										.findFirst();
								result.put(foundDevice.get(), measurement);
							}
						});

						return result;
					});
				});


		return deviceIdMeasurementMapFuture;
	}

	private void normalizeCoordinates(List<Borehole> boreholes, double baseHeight) {
		Optional<Double> minX = boreholes.stream()
				.flatMap(borehole -> borehole.points.stream())
				.map(point -> point.x)
				.min(Comparator.naturalOrder());

		if (minX.isPresent()) {
			boreholes.forEach(borehole -> {
				borehole.x = borehole.x - minX.get();
				borehole.points.forEach(point ->  {
					point.x = point.x - minX.get();
					point.y = point.y - baseHeight;
				});
			});
		}
	}

	private Map<Double, List<Double>> createLegend(String gradientId) {
		Map<Double, List<Double>> result = new LinkedHashMap<>();

		for (Double colorBoundary : gradientsUtil.getGradient().keySet()) {
			result.put(colorBoundary, asList(new Double[] {
					new Double(gradientsUtil.getGradient().get(colorBoundary).getR()),
					new Double(gradientsUtil.getGradient().get(colorBoundary).getG()),
					new Double(gradientsUtil.getGradient().get(colorBoundary).getB()),
					gradientsUtil.getValue(gradientId, colorBoundary)
			}));
		}

		return result;
	}

	private List<String> collectParameterIds() {
		List<String> result = new ArrayList<>();

		for(Device device : configuration.getProfileDevicesMap().get(
				configuration.getPickedProfile())) {
			for(String parameterId : device.getParameterIds()) {
				if(configuration.getParameterMap().get(parameterId) != null
						&& configuration.getParameterMap().get(parameterId).getMeasurementTypeName()
						.equals(configuration.getPickedParameterMeasurementName())) {
					result.add(parameterId);
				}
			}
		}

		return result;
	}

	private void setPointValuesAndColors(Map<Device, Measurement> deviceMeasurementMap,
			List<Borehole> boreholes, String parameterUnit, String gradientId,
			Optional<Measurement> externalOverride) {
		setupGradients(deviceMeasurementMap.values(), parameterUnit, gradientId);

		//setting values for all non-virtual boreholes first
		boreholes.stream().filter(borehole -> !borehole.virtual).forEach(borehole -> {
			if (borehole.points.size() > 2) {
				Measurement bottomMeasurement = deviceMeasurementMap.get(
						borehole.points.get(1).device);
				borehole.points.get(0).value = bottomMeasurement == null ? 0.0
						: bottomMeasurement.getValue();

				if (bottomMeasurement == null) {
					borehole.points.get(0).fakeValue = true;
				}

				if (externalOverride.isPresent()) {
					borehole.points.get(borehole.points.size() - 1).value =
							externalOverride.get().getValue();
					borehole.points.get(borehole.points.size() - 1).fakeValue = false;
				} else {
					Measurement topMeasurement = deviceMeasurementMap.get(
							borehole.points.get(borehole.points.size() - 2).device);
					double topValue = topMeasurement == null ? 0.0 : topMeasurement.getValue();
					borehole.points.get(borehole.points.size() - 1).value = topValue;

					if (topMeasurement == null) {
						borehole.points.get(borehole.points.size() - 1).fakeValue = true;
					}
				}

				IntStream.range(1, borehole.points.size() - 1).forEach(nextIndex -> {
					Measurement measurement = deviceMeasurementMap.get(
							borehole.points.get(nextIndex).device);
					borehole.points.get(nextIndex).value = measurement == null ? 0.0
							: measurement.getValue();

					if (measurement == null) {
						borehole.points.get(nextIndex).fakeValue = true;
					}
				});

				setRgbValues(borehole, gradientId);
			}
		});

		//setting values for all virtual boreholes
		IntStream.range(0, boreholes.size()).forEach(index -> {
			Borehole borehole = boreholes.get(index);

			if (borehole.virtual) {
				if (index == 0) {
					//first virtual borehole with one point
					if (externalOverride.isPresent()) {
						borehole.points.get(0).value = externalOverride.get().getValue();
						borehole.points.get(0).fakeValue = false;
					} else {
						Optional<Borehole> nextNonVirtual = boreholes.subList(1, boreholes.size())
								.stream()
								.filter(bh -> !bh.virtual)
								.findFirst();

						if (nextNonVirtual.isPresent()) {
							borehole.points.get(0).value = nextNonVirtual.get().points.get(1).value;
						} else {
							borehole.points.get(0).value = 0.0;
						}
					}
				} else if (index == boreholes.size() - 1) {
					//last virtual borehole with one point
					if (externalOverride.isPresent()) {
						borehole.points.get(0).value = externalOverride.get().getValue();
						borehole.points.get(0).fakeValue = false;
					} else {
						Optional<Borehole> previousNonVirtual = Lists.reverse(
								boreholes.subList(0, boreholes.size() - 1))
								.stream()
								.filter(bh -> !bh.virtual)
								.findFirst();

						if (previousNonVirtual.isPresent()) {
							borehole.points.get(0).value = previousNonVirtual.get().points.get(1).value;
						} else {
							borehole.points.get(0).value = 0.0;
						}
					}
				} else {
					//intermediary boreholes with many intermediary points
					Optional<Borehole> nextNonVirtual = boreholes
							.subList(index + 1, boreholes.size())
							.stream()
							.filter(bh -> !bh.virtual)
							.findFirst();
					Optional<Borehole> previousNonVirtual = Lists.reverse(
							boreholes.subList(0, index))
								.stream()
								.filter(bh -> !bh.virtual)
								.findFirst();

					if (externalOverride.isPresent()) {
						borehole.points.get(borehole.points.size() - 1).value =
								externalOverride.get().getValue();
					}

					if (previousNonVirtual.isPresent() && nextNonVirtual.isPresent()) {
						Point previousBottomPoint = previousNonVirtual.get().points.get(1);
						Point nextBottomPoint = nextNonVirtual.get().points.get(1);
						Point bottomPoint = borehole.points.get(0);
						bottomPoint.value = calculateValue(previousBottomPoint, nextBottomPoint,
								bottomPoint);

						if (!externalOverride.isPresent()) {
							Point previousTopPoint = previousNonVirtual.get().points.get(
									previousNonVirtual.get().points.size() - 2);
							Point nextTopPoint = nextNonVirtual.get().points.get(
									nextNonVirtual.get().points.size() - 2);
							Point topPoint = borehole.points.get(borehole.points.size() - 1);
							topPoint.value = calculateValue(previousTopPoint, nextTopPoint,
									topPoint);
						}

						//setting values for intermediary points
						IntStream.range(1, borehole.points.size() - 1).forEach(middlePointIndex -> {
							Point previousPoint = previousNonVirtual.get().points.get(Math.min(
									middlePointIndex,
									previousNonVirtual.get().points.size() - 2));
							Point nextPoint = nextNonVirtual.get().points.get(Math.min(
									middlePointIndex,
									nextNonVirtual.get().points.size() - 2));
							borehole.points.get(middlePointIndex).value = calculateValue(
									previousPoint, nextPoint,
									borehole.points.get(middlePointIndex));
						});
					} else if (!previousNonVirtual.isPresent() && nextNonVirtual.isPresent()) {
						borehole.points.get(0).value = nextNonVirtual.get().points.get(1).value;

						if (!externalOverride.isPresent()) {
							borehole.points.get(borehole.points.size() - 1).value =
									nextNonVirtual.get().points.get(
											nextNonVirtual.get().points.size() - 2).value;
						}

						//setting values for intermediary points
						IntStream.range(1, borehole.points.size() - 1).forEach(middlePointIndex -> {
							Point nextPoint = nextNonVirtual.get().points.get(Math.min(
									middlePointIndex,
									nextNonVirtual.get().points.size() - 2));
							borehole.points.get(middlePointIndex).value = nextPoint.value;
						});
					} else if (previousNonVirtual.isPresent() && !nextNonVirtual.isPresent()) {
						borehole.points.get(0).value = previousNonVirtual.get().points.get(1).value;

						if (!externalOverride.isPresent()) {
							borehole.points.get(borehole.points.size() - 1).value =
									previousNonVirtual.get().points.get(
											previousNonVirtual.get().points.size() - 2).value;
						}

						//setting values for intermediary points
						IntStream.range(1, borehole.points.size() - 1).forEach(middlePointIndex -> {
							Point previousPoint = previousNonVirtual.get().points.get(Math.min(
									middlePointIndex,
									previousNonVirtual.get().points.size() - 2));
							borehole.points.get(middlePointIndex).value = previousPoint.value;
						});
					} else {
						borehole.points.get(0).value = 0.0;

						if (!externalOverride.isPresent()) {
							borehole.points.get(borehole.points.size() - 1).value = 0.0;
						}

						//setting values for intermediary points
						IntStream.range(1, borehole.points.size() - 1).forEach(middlePointIndex -> {
							borehole.points.get(middlePointIndex).value = 0.0;
						});
					}
				}
			}

			setRgbValues(borehole, gradientId);
		});
	}

	private double calculateValue(Point previousPoint, Point nextPoint, Point currentPoint) {
		return previousPoint.value
				- (previousPoint.value - nextPoint.value)
				* ((currentPoint.x - previousPoint.x)
						/ (nextPoint.x - previousPoint.x));
	}

	private void setupGradients(Collection<Measurement> measurements, String parameterUnit,
			String gradientId) {
		double oldMinGradient = 0.0, oldMaxGradient = 0.0;
		boolean oldGradientExists = false;

		if (gradientsUtil.contains(gradientId)) {
			oldMinGradient = gradientsUtil.getMinValue(gradientId);
			oldMaxGradient = gradientsUtil.getMaxValue(gradientId);
			oldGradientExists = true;
		}

		measurements.forEach(measurement -> gradientsUtil.updateValues(
				gradientId, measurement.getValue()));

		if (oldGradientExists
				&& gradientsUtil.isExtended(gradientId, oldMinGradient, oldMaxGradient)) {
			eventBus.gradientExtended(gradientId);
		}
	}

	private void setRgbValues(Borehole borehole, String gradientId) {
		borehole.points.forEach(point -> {
			Color color = gradientsUtil.getColor(gradientId, point.value);
			point.r = color.getR();
			point.g = color.getG();
			point.b = color.getB();
		});
	}
}
