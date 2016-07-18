package pl.ismop.web.client.widgets.analysis.verticalslice;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.util.CoordinatesUtil;
import pl.ismop.web.client.util.GradientsUtil;
import pl.ismop.web.client.util.GradientsUtil.Color;
import pl.ismop.web.client.widgets.analysis.verticalslice.IVerticalSliceView.IVerticalSlicePresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = VerticalSliceView.class, multiple = true)
public class VerticalSlicePresenter extends BasePresenter<IVerticalSliceView, MainEventBus>
		implements IVerticalSlicePresenter, IPanelContent<IVerticalSliceView, MainEventBus> {
	
	private static class Point {
		
		double x, y, value;
		
		int r, g, b;
		
		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "Point [x=" + x + ", y=" + y + ", value=" + value + ", r=" + r + ", g=" + g
					+ ", b=" + b + "]";
		}
	}
	
	private static class Borehole {
		
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
//		if (!view.canRender()) {
//			eventBus.showSimpleError(view.cannotRenderMessage());
//			
//			return;
//		}
		
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
										device.getPlacement().getCoordinates().get(2));
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
									pickedProfile.getBaseHeight())),
							true)
					);
		});
		normalizeCoordinates(boreholes, pickedProfile.getBaseHeight());
		
		//adding profile's top end points
		Optional<Double> profileWidth = boreholes.stream()
				.flatMap(borehole -> borehole.points.stream())
				.map(point -> point.x)
				.max(Comparator.naturalOrder());
		double topLeftPoint = profileWidth.get() / 2 - PROFILE_TOP_WIDTH / 2;
		double topRightPoint = profileWidth.get() / 2 + PROFILE_TOP_WIDTH / 2;
		boreholes.add(new Borehole(topLeftPoint,
				Arrays.asList(
						new Point(topLeftPoint, PROFILE_HEIGHT),
						new Point(topLeftPoint, 0.0)),
				true));
		boreholes.add(new Borehole(topRightPoint,
				Arrays.asList(
						new Point(topRightPoint, PROFILE_HEIGHT),
						new Point(topRightPoint, 0.0)),
				true));
		
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
					nonVirtualBorehole.points.add(0, new Point(nonVirtualBorehole.x, 0.0));
					//adding top point
					nonVirtualBorehole.points.add(new Point(
							nonVirtualBorehole.x,
							leftBorehole.x - (leftBorehole.x - rightBorehole.x) / 2));
				}
			}
		});
		
		
		GWT.log("Point lists: " + boreholes);
		
		
		
		
//		ListenableFuture<List<DeviceAggregate>> aggregatesFuture = 
//				dapController.getDeviceAggregations(configuration.getPickedProfile().getId());
//		Futures.transform(aggregatesFuture, aggregates -> {
//			
//		});
		
		
		
		
		
//		List<String> parameterIds = collectParameterIds();
//		Parameter parameter = parameterIds.size() > 0 ?
//				configuration.getParameterMap().get(parameterIds.get(0)) : null;
//		String parameterUnit = parameter != null ? parameter.getMeasurementTypeUnit() : "";
//		String context = configuration.getDataSelector().equals("0") ? "measurements" : "scenarios";
//		ListenableFuture<List<Context>> contextFuture = dapController.getContext(context);
//		ListenableFuture<List<Timeline>> timelineFuture = Futures.transformAsync(contextFuture,
//				contextList -> {
//					if (contextList.size() > 0) {
//						return dapController.getTimelinesForParameterIds(
//								contextList.get(0).getId(), parameterIds);
//					}
//					
//					return Collections.emptyList();
//				});
		
		
		
		
		
//		dapController.getContext(context, new ContextsCallback() {
//			@Override
//			public void onError(ErrorDetails errorDetails) {
//				view.showLoadingState(false);
//				eventBus.showError(errorDetails);
//			}
//			
//			@Override
//			public void processContexts(List<Context> contexts) {
//				if(contexts.size() > 0) {
//					dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds,
//							new TimelinesCallback() {
//						@Override
//						public void onError(ErrorDetails errorDetails) {
//							view.showLoadingState(false);
//							eventBus.showError(errorDetails);
//						}
//						
//						@Override
//						public void processTimelines(final List<Timeline> timelines) {
//							if(!configuration.getDataSelector().equals("0")) {
//								for(Iterator<Timeline> i = timelines.iterator(); i.hasNext();) {
//									if(!i.next().getScenarioId().equals(
//											configuration.getDataSelector())) {
//										i.remove();
//									}
//								}
//							}
//							
//							List<String> timelineIds = new ArrayList<>();
//							
//							for(Timeline timeline : timelines) {
//								timelineIds.add(timeline.getId());
//							}
//							
//							Date queryDate = configuration.getDataSelector().equals("0")
//									? currentDate :	new Date(currentDate.getTime()
//											- configuration.getExperiment().getStart().getTime());
//							dapController.getLastMeasurementsWith24HourMod(timelineIds, queryDate,
//									new MeasurementsCallback() {
//								@Override
//								public void onError(ErrorDetails errorDetails) {
//									view.showLoadingState(false);
//									eventBus.showError(errorDetails);
//								}
//								
//								@Override
//								public void processMeasurements(List<Measurement> measurements) {
//									view.showLoadingState(false);
//									view.clear();
//									
//									if (measurements.size() > 0) {
//										gradientId = "analysis:" + parameterUnit;
//										
//										if (gradientsUtil.contains(gradientId)) {
//											gradientMin = gradientsUtil.getMinValue(gradientId);
//											gradientMax = gradientsUtil.getMaxValue(gradientId);
//										}
//										
//										for (Measurement measurement : measurements) {
//											gradientsUtil.updateValues(gradientId,
//													measurement.getValue());
//										}
//										
//										//TODO: come up with a better way of telling where the water is
//										boolean leftBank = false;
//										
//										if (configuration.getPickedProfile().getShape()
//												.getCoordinates().get(0).get(0) > 19.676851838778) {
//											leftBank = true;
//										}
//										
//										view.init();
//										view.drawCrosssection(parameterUnit, leftBank,
//											calculateProfileAndDevicePositionsWithValuesAndColors(
//													measurements,
//													timelines,
//													configuration.getPickedProfile(),
//													parameterUnit), createLegend(gradientId));
//										
//										if (gradientsUtil.isExtended(gradientId, gradientMin,
//												gradientMax)) {
//											gradientMin = gradientsUtil.getMinValue(gradientId);
//											gradientMax = gradientsUtil.getMaxValue(gradientId);
//											eventBus.gradientExtended(gradientId);
//										}
//									} else {
//										eventBus.showSimpleError(view.noMeasurementsMessage());
//									}
//								}
//							});
//						}
//					});
//				}
//			}
//		});
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
	
	/**
	 * 
	 * @param parameterUnit 
	 * @return Map<x coordinate, List<value, color R, color G, color B>>
	 */
	private Map<Double, List<Double>> calculateProfileAndDevicePositionsWithValuesAndColors(
			List<Measurement> measurements, List<Timeline> timelines, Profile profile,
			String parameterUnit) {
		Map<Double, List<Double>> result = new LinkedHashMap<>();
		List<List<Double>> toBeProjected = new ArrayList<>();
		toBeProjected.addAll(profile.getShape().getCoordinates());
		
		List<Device> devices = configuration.getProfileDevicesMap().get(profile);
		Collections.sort(devices, new Comparator<Device>() {
			@Override
			public int compare(Device o1, Device o2) {
				return o1.getPlacement().getCoordinates().get(0).compareTo(
						o2.getPlacement().getCoordinates().get(0));
			}
		});
		
		for (Device device : devices) {
			toBeProjected.add(device.getPlacement().getCoordinates());
		}
		
		Collections.sort(toBeProjected, new Comparator<List<Double>>() {
			@Override
			public int compare(List<Double> o1, List<Double> o2) {
				return o1.get(0).compareTo(o2.get(0));
			}
		});
		
		List<List<Double>> projected = coordinatesUtil.projectCoordinates(toBeProjected);
		double minX = projected.get(0).get(0);
		
		for (List<Double> point : projected) {
			point.set(0, point.get(0) - minX);
		}
		
		for (int i = 0; i < projected.size(); i++) {
			List<Double> valueAndColors = new ArrayList<>();
			
			if (i == 0) {
				//left profile coordinate
				double value = getDeviceValue(devices.get(0), measurements, timelines);
				valueAndColors.add(value);
				completeColors(valueAndColors, value, "analysis:" + parameterUnit);
				result.put(projected.get(i).get(0), valueAndColors);
			} else if (i == projected.size() - 1) {
				//right profile coordinate
				double value = getDeviceValue(devices.get(devices.size() - 1), measurements,
						timelines);
				valueAndColors.add(value);
				completeColors(valueAndColors, value, "analysis:" + parameterUnit);
				result.put(projected.get(i).get(0), valueAndColors);
			} else {
				//device coordinates
				double value = getDeviceValue(devices.get(i - 1), measurements, timelines);
				valueAndColors.add(value);
				completeColors(valueAndColors, value, "analysis:" + parameterUnit);
				result.put(projected.get(i).get(0), valueAndColors);
			}
		}
		
		return result;
	}

	private void completeColors(List<Double> valueAndColors, double value, String gradientId) {
		Color color = gradientsUtil.getColor(gradientId, value);
		valueAndColors.add(new Integer(color.getR()).doubleValue());
		valueAndColors.add(new Integer(color.getG()).doubleValue());
		valueAndColors.add(new Integer(color.getB()).doubleValue());
	}

	private Double getDeviceValue(Device device, List<Measurement> measurements,
			List<Timeline> timelines) {
		for (Timeline timeline : timelines) {
			if (device.getParameterIds().contains(timeline.getParameterId())) {
				for (Measurement measurement : measurements) {
					if (measurement.getTimelineId().equals(timeline.getId())) {
						return measurement.getValue();
					}
				}
			}
		}
		
		return 0.0;
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
}