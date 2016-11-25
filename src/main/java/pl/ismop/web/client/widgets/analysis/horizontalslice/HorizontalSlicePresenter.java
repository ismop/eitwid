package pl.ismop.web.client.widgets.analysis.horizontalslice;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.util.function.Function.identity;
import static javaslang.API.Tuple;

import java.util.Date;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.Scheduler;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Value;
import javaslang.collection.Iterator;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.collection.Set;
import javaslang.concurrent.Future;
import javaslang.control.Option;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.FunctionalDapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorUtil;
import pl.ismop.web.client.util.CoordinatesUtil;
import pl.ismop.web.client.util.GradientsUtil;
import pl.ismop.web.client.util.GradientsUtil.Color;
import pl.ismop.web.client.widgets.analysis.horizontalslice.IHorizontalSliceView.IHorizontalSlicePresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = HorizontalSliceView.class, multiple = true)
public class HorizontalSlicePresenter extends BasePresenter<IHorizontalSliceView, MainEventBus>
		implements IHorizontalSlicePresenter,
		IPanelContent<IHorizontalSliceView, MainEventBus> {

	private static final Logger log = LoggerFactory.getLogger(HorizontalSlicePresenter.class);

	private HorizontalCrosssectionConfiguration configuration;

	private ISelectionManager selectionManager;

	private CoordinatesUtil coordinatesUtil;

	private double shiftX, shiftY, scale, panX;

	private Date currentDate;

	private GradientsUtil gradientsUtil;

	private double gradientMin, gradientMax;

	private String gradientId;

	private FunctionalDapController dapController;

	private ErrorUtil errorUtil;

	@Inject
	public HorizontalSlicePresenter(FunctionalDapController dapController,
			CoordinatesUtil coordinatesUtil,
			GradientsUtil gradientsUtil, ErrorUtil errorUtil) {
		this.dapController = dapController;
		this.coordinatesUtil = coordinatesUtil;
		this.gradientsUtil = gradientsUtil;
		this.errorUtil = errorUtil;
	}

	public void onUpdateHorizontalSliceConfiguration(
			HorizontalCrosssectionConfiguration configuration) {
		if (this.configuration == configuration) {
			refreshView();
		}

		addSectionsToMinimap();
	}

	public void onDateChanged(Date selectedDate) {
		this.currentDate = selectedDate;
		refreshView();
	}

	@Override
	public void setSelectedExperiment(Experiment experiment) {
		//not needed for now
	}

	@Override
	public void setSelectedDate(Date date) {
		this.currentDate = date;
		Scheduler.get().scheduleDeferred(() -> {
			refreshView();
		});
	}

	@Override
	public void edit() {
		eventBus.showHorizontalCrosssectionWizardWithConfig(configuration);
	}

	public void setConfiguration(HorizontalCrosssectionConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void setSelectionManager(ISelectionManager selectionManager) {
		this.selectionManager = selectionManager;
		Scheduler.get().scheduleDeferred(() -> {
			addSectionsToMinimap();
		});
	}

	public void onGradientExtended(String gradientId) {
		if (gradientId.equals(this.gradientId)
				&& gradientsUtil.isExtended(gradientId, gradientMin, gradientMax)) {
			refreshView();
		}
	}

	@Override
	public void destroy() {
		//not used
	}

	private void addSectionsToMinimap() {
		selectionManager.clear();
		configuration.getPickedSectionIds()
			.map(sectionId -> configuration.getSections().get(sectionId))
			.filter(Option::isDefined)
			.map(Option::get)
			.forEach(selectionManager::add);
	}

	private void refreshView() {
		if (!view.canRender()) {
			eventBus.showSimpleError(view.cannotRenderMessages());

			return;
		}

		view.showLoadingState(true);

		Seq<Parameter> parameters = configuration.getParametersById().values()
			.filter(parameter -> parameter.getMeasurementTypeName().equals(
					configuration.getPickedParameterName()));
		String contextType = configuration.getPickedScenarioId().equals("0")
				? "measurements" : "scenarios";
		String parameterUnit = parameters.get(0).getMeasurementTypeUnit();
		dapController.getContexts(contextType).flatMap(contexts -> {
			if (contexts.size() > 0) {
				return dapController.getTimelinesForParameterIds(contexts.get(0).getId(),
						parameters.map(Parameter::getId));
			} else {
				return Future.failed(new Exception("Context for type " + contextType
						+ " could not be retirevied"));
			}
		}).map(timelines -> timelines.filter(timeline ->
			configuration.getPickedScenarioId().equals("0")
				|| configuration.getPickedScenarioId().equals(timeline.getScenarioId()))
		).map(timelines -> timelines.toMap(timeline ->
				Tuple(configuration.getParametersById().get(timeline.getParameterId())
						.get().getDeviceId(), timeline))
		).flatMap(timelinesByDeviceId -> {
			Date queryDate = configuration.getPickedScenarioId().equals("0")
				? currentDate
				: new Date(currentDate.getTime() - configuration.getExperiment().getStart()
						.getTime());
			return dapController.getLastMeasurementsWith24HourMod(
					timelinesByDeviceId.values().map(Timeline::getId), queryDate)
				.map(measurements -> {
					Map<String, String> deviceIdsByTimelineId = timelinesByDeviceId
							.map((deviceId, timeline) -> Tuple(timeline.getId(), deviceId));

					return measurements.<String, Measurement>toMap(measurement ->
						Tuple(deviceIdsByTimelineId.get(measurement.getTimelineId()).get(),
								measurement));
				});
		}).onSuccess(measurementsByDeviceId -> {
			view.showLoadingState(false);
			view.init();
			view.clear();
			drawMuteSections(configuration.getSections().values(),
					configuration.getSections().values()
						.filter(section -> !configuration.getPickedSectionIds()
								.contains(section.getId())));
			gradientId = "analysis:" + parameterUnit;
			updateGradient(measurementsByDeviceId.values());
			view.drawCrosssection(createLegend(gradientId), parameterUnit,
					createLocationsWithColors(measurementsByDeviceId, gradientId));
		}).onFailure(e -> {
			view.showLoadingState(false);
			eventBus.showError(errorUtil.processErrors(null, e));
		});
	}

	private void updateGradient(Seq<Measurement> measurements) {
		if (gradientsUtil.contains(gradientId)) {
			gradientMin = gradientsUtil.getMinValue(gradientId);
			gradientMax = gradientsUtil.getMaxValue(gradientId);
		}

		measurements.map(Measurement::getValue).forEach(value ->
			gradientsUtil.updateValues(gradientId, value));

		if (gradientsUtil.isExtended(gradientId, gradientMin, gradientMax)) {
			gradientMin = gradientsUtil.getMinValue(gradientId);
			gradientMax = gradientsUtil.getMaxValue(gradientId);
			eventBus.gradientExtended(gradientId);
		}
	}

	/**
	 * @return Map<section_id, Seq<Map<Tuple3<x, y, virtual>, Tuple3<r, g, b>>>> sequence for
	 * profiles (including virtual ones representing section boundaries), map for each device in
	 * a profile (including the virtual ones on section boundaries)
	 */
	private Map<String, Seq<? extends Map<Tuple3<Double, Double, Boolean>,
			Tuple3<Integer, Integer, Integer>>>> createLocationsWithColors(
					Map<String, Measurement> measurementsByDeviceId, String gradientId) {

		Map<String, Device> devicesById = configuration.getDevicesBySectionId().values()
				.flatMap(identity())
				.toMap(device -> Tuple(device.getId(), device));

		Map<String, Seq<? extends Map<Tuple3<Double, Double, Boolean>,
				Tuple3<Integer, Integer, Integer>>>> result =
		measurementsByDeviceId.keySet()
				.map(deviceId -> devicesById.get(deviceId))
				.filter(Option::isDefined)
				.map(Option::get)
				.groupBy(Device::getSectionId)
				.map((sectionId, devices) -> Tuple(
						sectionId,
						devicesToLocations(devices, measurementsByDeviceId)));

		//adding virtual devices on section boundaries
		result = result.map((sectionId, profiles) -> Tuple(
				sectionId,
				profiles.map(profile -> addProfileBoundaries(
						configuration.getSections().get(sectionId), profile))));

		//adding virtual profiles for section side boundaries
		result = addVirtualProfiles(result);

		return result;
	}

	private Map<String, Seq<? extends Map<Tuple3<Double, Double, Boolean>,
			Tuple3<Integer, Integer, Integer>>>> addVirtualProfiles(
			Map<String, Seq<? extends Map<Tuple3<Double, Double, Boolean>,
					Tuple3<Integer, Integer, Integer>>>> profilesBySectionId) {
		return profilesBySectionId.map((sectionId, profiles) ->
				Tuple(sectionId, addVirtualProfiles(sectionId, profiles)));
	}

	private Seq<? extends Map<Tuple3<Double, Double, Boolean>,
			Tuple3<Integer, Integer, Integer>>> addVirtualProfiles(String sectionId,
			Seq<? extends Map<Tuple3<Double, Double, Boolean>,
					Tuple3<Integer, Integer, Integer>>> profiles) {
		Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>> firstProfile =
				profiles.head();
		Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>> lastProfile =
				profiles.last();
		Section section = configuration.getSections().get(sectionId).get();
		Seq<Map<Tuple3<Double, Double, Boolean>,
				Tuple3<Integer, Integer, Integer>>> result = profiles.map(identity());

		return result
				.prepend(createLeftProfile(section, firstProfile))
				.append(createRightProfile(section, lastProfile));
	}

	private Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>>
		createLeftProfile(Section section,
			Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>> profile) {

		Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> topBoundary =
				getSectionTopBoundary(section);

		return profile.map((location, color) ->
				Tuple(shiftLocation(location, topBoundary, topBoundary._1()), color));
	}

	private Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>>
		createRightProfile(Section section,
			Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>> profile) {

		Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> topBoundary =
				getSectionTopBoundary(section);

		return profile.map((location, color) ->
				Tuple(shiftLocation(location, topBoundary, topBoundary._2()), color));
	}

	private Tuple3<Double, Double, Boolean> shiftLocation(Tuple3<Double, Double, Boolean> location,
			Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> boundary,
			Tuple2<Double, Double> referencePoint) {

		Seq<Double> firstPoint = List.of(boundary._1()._1(), boundary._1()._2());
		Seq<Double> secondPoint = List.of(boundary._2()._1(), boundary._2()._2());
		//calculating the a coefficient of the section boundary (y = ax + b)
		double a = (secondPoint.get(1) - firstPoint.get(1))
				/ (secondPoint.get(0) - firstPoint.get(0));
		//calculating d coefficient of of a line parallel to boundary (y = ax + d)
		double d = location._2() - a * location._1();
		//calculating f coefficient of a perpendicular line to the first one crossing
		//the reference point
		double f = referencePoint._2() + referencePoint._1() / a;
		//calculating the intersection point of the perpendicular and parallel line
		double x = (f - d) / (a + (1 / a));
		double y = a * ((f - d) / (a + (1 / a))) + d;

		return Tuple(x, y, false);
	}

	private Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>>
		addProfileBoundaries(Option<Section> section,
			Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>> profile) {

		if (section.isDefined()) {
			List<Tuple3<Double, Double, Boolean>> locationList = profile.keySet().toList();
			Tuple3<Double, Double, Boolean> topLocation = locationList.head();
			Tuple3<Double, Double, Boolean> bottomLocation = locationList.last();
			Tuple3<Integer, Integer, Integer> topValue = profile.get(topLocation).get();
			Tuple3<Integer, Integer, Integer> bottomValue = profile.get(bottomLocation).get();
			Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> sectionsTopBoundary =
					getSectionTopBoundary(section.get());
			Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> sectionsBottomBoundary =
					getSectionBottomBoundary(section.get());

			return profile
					.toList()
					.prepend(calculateVirtualLocation(topLocation, topValue, sectionsTopBoundary))
					.append(calculateVirtualLocation(bottomLocation, bottomValue,
							sectionsBottomBoundary))
					.toLinkedMap(identity());
		}

		throw new IllegalArgumentException("Missing section while creating virtual devices");
	}

	private Tuple2<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>>
		calculateVirtualLocation(
			Tuple3<Double, Double, Boolean> referenceLocation,
			Tuple3<Integer, Integer, Integer> referenceValue,
			Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> sectionsBoundary) {

		Seq<Double> firstPoint = List.of(sectionsBoundary._1()._1(), sectionsBoundary._1()._2());
		Seq<Double> secondPoint = List.of(sectionsBoundary._2()._1(), sectionsBoundary._2()._2());
		//calculating the a coefficient of the section boundary (y = ax + b)
		double a = (secondPoint.get(1) - firstPoint.get(1))
				/ (secondPoint.get(0) - firstPoint.get(0));
		//calculating b coefficient of the section boundary (y = ax + b)
		double b = firstPoint.get(1) - a * firstPoint.get(0);
		//calculating f coefficient of a perpendicular line to the first one crossing
		//the reference point
		double f = referenceLocation._2() + referenceLocation._1() / a;
		//calculating the intersection point of the perpendicular and parallel line
		double x = (f - b) / (a + (1 / a));
		double y = a * ((f - b) / (a + (1 / a))) + b;

		return Tuple(Tuple(x, y, false), referenceValue);
	}

	private Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> getSectionBottomBoundary(
			Section section) {

		List<List<Double>> points = List.ofAll(section.getShape().getCoordinates())
				.map(point -> List.ofAll(point))
				.removeAt(0);
		assert points.size() == 4 : "Number of section corners has to be exactly 4";
		Seq<Seq<Double>> convertedPoints = convertPoints(points);
		Iterator<? extends Seq<? extends Seq<Double>>> sortedPoints = convertedPoints
			.sorted((point1, point2) -> point1.get(0).compareTo(point2.get(0)))
			.grouped(2);
		Seq<? extends Seq<Double>> leftPoints = sortedPoints.next().sorted((point1, point2) ->
				point1.get(1).compareTo(point2.get(1)));
		Seq<? extends Seq<Double>> rightPoints = sortedPoints.next().sorted((point1, point2) ->
		point1.get(1).compareTo(point2.get(1)));

		return Tuple(Tuple(leftPoints.get(1).get(0), leftPoints.get(1).get(1)),
				Tuple(rightPoints.get(1).get(0), rightPoints.get(1).get(1)));
	}

	private Tuple2<Tuple2<Double, Double>, Tuple2<Double, Double>> getSectionTopBoundary(
			Section section) {

		List<List<Double>> points = List.ofAll(section.getShape().getCoordinates())
				.map(point -> List.ofAll(point))
				.removeAt(0);
		assert points.size() == 4 : "Number of section corners has to be exactly 4";
		Seq<Seq<Double>> convertedPoints = convertPoints(points);
		Iterator<? extends Seq<? extends Seq<Double>>> sortedPoints = convertedPoints
			.sorted((point1, point2) -> point1.get(0).compareTo(point2.get(0)))
			.grouped(2);
		Seq<? extends Seq<Double>> leftPoints = sortedPoints.next().sorted((point1, point2) ->
				point1.get(1).compareTo(point2.get(1)));
		Seq<? extends Seq<Double>> rightPoints = sortedPoints.next().sorted((point1, point2) ->
		point1.get(1).compareTo(point2.get(1)));

		return Tuple(Tuple(leftPoints.get(0).get(0), leftPoints.get(0).get(1)),
				Tuple(rightPoints.get(0).get(0), rightPoints.get(0).get(1)));
	}

	private Seq<Seq<Double>> convertPoints(List<List<Double>> points) {
		return rotate(List.ofAll(
					coordinatesUtil.projectCoordinates(points.map(Value::toJavaList).toJavaList()))
						.map(List::ofAll))
				.map(point -> List.of(
						(point.get(0) - shiftX) * scale + panX,
						(point.get(1) - shiftY) * scale));
	}

	private Seq<? extends Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>>>
			devicesToLocations(Set<Device> devices,
					Map<String, Measurement> measurementsByDeviceId) {
		return devices.groupBy(Device::getProfileId)
				.toList()
				.map(profileDeviceTuple -> profileDeviceTuple._2())
				.map(profileDevices -> profileDevices.toSortedMap(
						this::compareLocationsVertically,
						device -> Tuple(
							deviceToPosition(device),
							measurementToColor(measurementsByDeviceId.get(device.getId())))))
				.sorted(this::compareLocationMaps);
	}

	private int compareLocationsVertically(Tuple3<Double, Double, Boolean> location1,
			Tuple3<Double, Double, Boolean> location2) {

		return location1._2().compareTo(location2._2());
	}

	private int compareLocationMaps(
			Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>> locations1,
			Map<Tuple3<Double, Double, Boolean>, Tuple3<Integer, Integer, Integer>> locations2) {

		return locations1.keySet().get()._1().compareTo(locations2.keySet().get()._1());
	}

	private Tuple3<Integer, Integer, Integer> measurementToColor(Option<Measurement> measurement) {
		return measurement
				.map(m -> {
					Color color = gradientsUtil.getColor(gradientId, m.getValue());

					return Tuple(color.getR(), color.getG(), color.getB());
				})
				.getOrElse(Tuple(0, 0, 0));
	}

	private Tuple3<Double, Double, Boolean> deviceToPosition(Device device) {
		Seq<Seq<Double>> projectedCoordinates = List.ofAll(
				coordinatesUtil.projectCoordinates(List.of(
						device.getPlacement().getCoordinates()).toJavaList()))
				.map(point -> List.ofAll(point));
		Seq<Seq<Double>> shiftedAndRotatedCoordinates = rotate(projectedCoordinates)
				.map(point -> List.of(point.get(0) - shiftX, point.get(1) - shiftY));

		Seq<Seq<Seq<Double>>> scaledAndShiftedCoordinates = scaleAndShift(List.of(
				shiftedAndRotatedCoordinates), scale, panX);

		return Tuple(
				scaledAndShiftedCoordinates.get(0).get(0).get(0),
				scaledAndShiftedCoordinates.get(0).get(0).get(1),
				true);
	}

	private void drawMuteSections(Seq<Section> allSections, Seq<Section> muteSections) {
		Seq<Seq<Seq<Double>>> coordinates = List.empty();
		double	maxX = Double.MIN_VALUE,
				maxY = Double.MIN_VALUE;
		shiftX = Double.MAX_VALUE;
		shiftY = Double.MAX_VALUE;

		for (Section section : muteSections) {
			if (section.getShape() != null) {
				Seq<Seq<Double>> projected = List.ofAll(coordinatesUtil.projectCoordinates(
						section.getShape().getCoordinates()))
							.map(list -> List.ofAll(list));
				projected = rotate(projected);
				coordinates = coordinates.append(projected);

				for (Seq<Double> point : projected) {
					if (point.get(0) > maxX) {
						maxX = point.get(0);
					}

					if (point.get(0) < shiftX) {
						shiftX = point.get(0);
					}

					if (point.get(1) > maxY) {
						maxY = point.get(1);
					}

					if (point.get(1) < shiftY) {
						shiftY = point.get(1);
					}
				}
			}
		}

		coordinates = coordinates.map(
				points -> points.map(
						point -> List.of(point.get(0) - shiftX, point.get(1) - shiftY)));
		panX = 200;
		scale = computeScale(coordinates, panX, view.getHeight(), view.getWidth());
		coordinates = scaleAndShift(coordinates, scale, panX);
		view.drawScale(scale, panX);
		view.drawMuteSections(coordinates);
	}

	private double computeScale(Seq<Seq<Seq<Double>>> coordinates, double panX, double height,
			double width) {
		double 	minY = Double.MAX_VALUE,
				maxY = Double.MIN_VALUE,
				minX = Double.MAX_VALUE,
				maxX = Double.MIN_VALUE;

		for (Seq<Seq<Double>> sectionCoordinates : coordinates) {
			for (Seq<Double> point : sectionCoordinates) {
				if(point.get(0) > maxX) {
					maxX = point.get(0);
				}

				if(point.get(0) < minX) {
					minX = point.get(0);
				}

				if(point.get(1) > maxY) {
					maxY = point.get(1);
				}

				if(point.get(1) < minY) {
					minY = point.get(1);
				}
			}
		}

		double sectionsHeight = abs(maxY - minY);
		double sectionsWidth = abs(maxX - minX);

		return min(height / sectionsHeight, (width - panX) / sectionsWidth);
	}

	private Seq<Seq<Seq<Double>>> scaleAndShift(Seq<Seq<Seq<Double>>> coordinates,
			double scale, double panX) {

		return coordinates.map(points -> points.map(
				point -> List.of(point.get(0) * scale + panX, point.get(1) * scale)));
	}

	private Seq<Seq<Double>> rotate(Seq<Seq<Double>> points) {
		return points.map(point ->
			List.of(point.get(0) * cos(PI / 2) - point.get(1) * sin(PI / 2),
					point.get(0) * sin(PI / 2) + point.get(1) * cos(PI / 2))
		);
	}

	private Map<Double, Seq<Double>> createLegend(String gradientId) {
		return List.ofAll(gradientsUtil.getGradient().keySet())
			.toLinkedMap(colorBoundary ->
				Tuple(colorBoundary, List.of(
					new Double(gradientsUtil.getGradient().get(colorBoundary).getR()),
					new Double(gradientsUtil.getGradient().get(colorBoundary).getG()),
					new Double(gradientsUtil.getGradient().get(colorBoundary).getB()),
					new Double(gradientsUtil.getValue(gradientId, colorBoundary))
				))
			);
	}
}
