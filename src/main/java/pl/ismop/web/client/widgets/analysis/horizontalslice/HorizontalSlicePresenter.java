package pl.ismop.web.client.widgets.analysis.horizontalslice;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import java.util.Date;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.Scheduler;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import javaslang.API;
import javaslang.collection.HashMap;
import javaslang.collection.HashSet;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.control.Option;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.FunctionalDapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorUtil;
import pl.ismop.web.client.util.CoordinatesUtil;
import pl.ismop.web.client.util.GradientsUtil;
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

		Scheduler.get().scheduleDeferred(() -> {
			view.showLoadingState(true);
			view.init();
			drawMuteSections(configuration.getSections().values(),
					configuration.getSections().values()
					.filter(section -> !configuration.getPickedSectionIds()
							.contains(section.getId())));
		});






		Seq<String> parameterIds = List.empty();
		Parameter parameter = null;

//		for (String height : configuration.getPickedHeights().values()) {
//			for (Device device : configuration.getHeightDevicesmap().get(height)) {
//				for (String parameterId : device.getParameterIds()) {
//					if (configuration.getParameterMap().get(parameterId) != null
//							&& configuration.getParameterMap().get(parameterId)
//							.getMeasurementTypeName().equals(
//									configuration.getPickedParameterMeasurementName())) {
//						parameterIds.add(parameterId);
//						parameter = configuration.getParameterMap().get(parameterId);
//					}
//				}
//			}
//		}

		final String parameterUnit = parameter != null ? parameter.getMeasurementTypeUnit() : "";
		String context = "";//configuration.getDataSelector().equals("0") ? "measurements" : "scenarios";
//		dapController.getContexts(context)
//			.flatMap(contexts -> {
//				if (contexts.size() > 0) {
//					return dapController.getTimelinesForParameterIds(contexts.get(0).getId(),
//						javaslang.collection.List.ofAll(parameterIds));
//				} else {
//					return Future.failed(new IllegalArgumentException("No context is present"));
//				}
//			})
//			.map(timelines -> {
//				if (!configuration.getDataSelector().equals("0")) {
//					return timelines.filter(timeline -> timeline.getScenarioId().equals(
//							configuration.getDataSelector()));
//				} else {
//					return timelines;
//				}
//			})
//			.flatMap(timelines -> {
//				Date queryDate = configuration.getDataSelector().equals("0")
//						? currentDate :
//						new Date(currentDate.getTime()
//								- configuration.getExperiment().getStart().getTime());
//				return dapController.getLastMeasurementsWith24HourMod(
//							timelines.map(Timeline::getId), queryDate)
//						.map(measurements -> {
//							javaslang.collection.Map<String, Timeline> timelineMap = timelines
//									.toMap(timeline -> Tuple.of(timeline.getId(), timeline));
//								javaslang.collection.Map<Timeline, ? extends Seq<Measurement>> result =
//										measurements.groupBy(measurement ->
//										timelineMap.get(measurement.getTimelineId()).get());
//
//							return result;
//						});
//			})
//			.onFailure(e -> {
//				view.showLoadingState(false);
//				eventBus.showError(errorUtil.processErrors(null, e));
//			})
//			.onSuccess(measurements -> {
//				view.showLoadingState(false);
//				view.clear();
//
//				if (measurements.size() > 0) {
//					Seq<Section> muteSections = javaslang.collection.List.ofAll(
//							configuration.getSections().values())
//							.filter(section -> !configuration.getPickedSections()
//									.containsKey(section.getId())
//									|| section.getShape().getCoordinates().size() >= 6);
//					gradientId = "analysis:" + parameterUnit;
//
//					if (gradientsUtil.contains(gradientId)) {
//						gradientMin = gradientsUtil.getMinValue(gradientId);
//						gradientMax = gradientsUtil.getMaxValue(gradientId);
//					}
//
//					for (Measurement measurement : measurements.values()
//							.flatMap(Function.identity())) {
//						gradientsUtil.updateValues(gradientId,
//								measurement.getValue());
//					}
//
//					view.init();
//					drawMuteSections(configuration.getSections().values(),
//							muteSections.toJavaList());
//					view.drawCrosssection(createLegend(gradientId),
//							parameterUnit,
//							createDeviceLocationsWithValuesAndColors(
//									measurements, gradientId));
//
//					if (gradientsUtil.isExtended(gradientId, gradientMin,
//							gradientMax)) {
//						gradientMin = gradientsUtil.getMinValue(gradientId);
//						gradientMax = gradientsUtil.getMaxValue(gradientId);
//						eventBus.gradientExtended(gradientId);
//					}
//				} else {
//					eventBus.showSimpleError(view.noMeasurementsMessage());
//				}
//			});
	}

	@Override
	public void destroy() {
		//not used
	}

	private Map<List<List<Double>>, Map<List<Double>, List<Double>>>
			createDeviceLocationsWithValuesAndColors(
					javaslang.collection.Map<Timeline, ? extends Seq<Measurement>> timelineMeasurementMap,
					String gradientId) {
		Map<List<List<Double>>, Map<List<Double>, List<Double>>> result = HashMap.empty();

//		for (Section section : configuration.getPickedSections().values()) {
//			Map<List<Double>, Double> temp = new LinkedHashMap<>();
//			List<List<Double>> keys = new ArrayList<>();
//			Double value = 0.0;
//
//			for (Device device : configuration.getSectionDevicesMap().get(section)) {
//				PARAMETER:
//				for (Parameter parameter : configuration.getParameterMap().values()) {
//					if (device.getParameterIds().contains(parameter.getId())
//							&& parameter.getMeasurementTypeName().equals(
//									configuration.getPickedParameterMeasurementName())) {
//						for (Timeline timeline : timelineMeasurementMap.keySet()) {
//							if (parameter.getTimelineIds().contains(timeline.getId())) {
//								for (Measurement measurement : timelineMeasurementMap.get(timeline).get()) {
//									value = measurement.getValue();
//
//									break PARAMETER;
//								}
//							}
//						}
//					}
//				}
//
//				if (device.getPlacement() != null
//						&& device.getPlacement().getCoordinates() != null) {
//					List<List<Double>> coordinates = new ArrayList<>();
//					coordinates.add(device.getPlacement().getCoordinates());
//
//					List<List<Double>> projectedCoordinates = coordinatesUtil.projectCoordinates(
//							coordinates);
//					rotate(projectedCoordinates);
//
//					for (List<Double> pointCoordinates : projectedCoordinates) {
//						pointCoordinates.set(0, pointCoordinates.get(0) - shiftX);
//						pointCoordinates.set(1, pointCoordinates.get(1) - shiftY);
//					}
//
//					List<List<List<Double>>> toBeScaledAndShiftedCoordinates = new ArrayList<>();
//					toBeScaledAndShiftedCoordinates.add(projectedCoordinates);
//					scaleAndShift(toBeScaledAndShiftedCoordinates, scale, panX);
//					temp.put(toBeScaledAndShiftedCoordinates.get(0).get(0), value);
//					keys.add(toBeScaledAndShiftedCoordinates.get(0).get(0));
//				}
//			}
//
//			sort(keys, new Comparator<List<Double>>() {
//				@Override
//				public int compare(List<Double> o1, List<Double> o2) {
//					return -o1.get(1).compareTo(o2.get(1));
//				}
//			});
//
//			Map<List<Double>, List<Double>> locationsWithReadings = new LinkedHashMap<>();
//
//			for (List<Double> key : keys) {
//				Double finalValue = temp.get(key);
//				Color color = gradientsUtil.getColor(gradientId, finalValue);
//				List<Double> valueWithColor = new ArrayList<>();
//				valueWithColor.add(finalValue);
//				valueWithColor.add(new Integer(color.getR()).doubleValue());
//				valueWithColor.add(new Integer(color.getG()).doubleValue());
//				valueWithColor.add(new Integer(color.getB()).doubleValue());
//				locationsWithReadings.put(key, valueWithColor);
//			}
//
//			//removing last element which is just there to close the loop
//			List<List<Double>> corners = section.getShape().getCoordinates()
//					.subList(0, section.getShape().getCoordinates().size() - 1);
//			List<List<Double>> projectedCorners = coordinatesUtil.projectCoordinates(corners);
//			rotate(projectedCorners);
//
//			for (List<Double> pointCoordinates : projectedCorners) {
//				pointCoordinates.set(0, pointCoordinates.get(0) - shiftX);
//				pointCoordinates.set(1, pointCoordinates.get(1) - shiftY);
//			}
//
//			List<List<List<Double>>> scaledAndShiftedCoordinates = new ArrayList<>();
//			scaledAndShiftedCoordinates.add(projectedCorners);
//			scaleAndShift(scaledAndShiftedCoordinates, scale, panX);
//			List<List<Double>> scaled = scaledAndShiftedCoordinates.get(0);
//			sort(scaled, new Comparator<List<Double>>() {
//				@Override
//				public int compare(List<Double> o1, List<Double> o2) {
//					return -o1.get(1).compareTo(o2.get(1));
//				}
//			});
//
//			if (scaled.size() > 3) {
//				if (scaled.get(0).get(0) > scaled.get(1).get(0)) {
//					scaled.add(0, scaled.remove(1));
//				}
//
//				if (scaled.get(2).get(0) < scaled.get(3).get(0)) {
//					scaled.add(2, scaled.remove(3));
//				}
//			}
//
//			result.put(scaled , locationsWithReadings);
//		}

		return result;
	}

	private void drawMuteSections(Seq<Section> allSections, Seq<Section> muteSections) {
		log.info("Drawing {} mute sections of {} all sections",
				muteSections.size(), allSections.size());

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
		view.drawMuteSections(coordinates.map(points -> points.map(
				point -> point.toJavaList()).toJavaList()).toJavaList());
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

	private Map<Double, List<Double>> createLegend(String gradientId) {
		return HashSet.ofAll(gradientsUtil.getGradient().keySet()).toMap(colorBoundary ->
			API.Tuple(colorBoundary, List.of(
				new Double(gradientsUtil.getGradient().get(colorBoundary).getR()),
				new Double(gradientsUtil.getGradient().get(colorBoundary).getG()),
				new Double(gradientsUtil.getGradient().get(colorBoundary).getB()),
				gradientsUtil.getValue(gradientId, colorBoundary)
			))
		);
	}
}
