package pl.ismop.web.client.widgets.analysis.horizontalslice;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ContextsCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.TimelinesCallback;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
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
	private HorizontalCrosssectionConfiguration configuration;
	
	private ISelectionManager selectionManager;

	private DapController dapController;

	private CoordinatesUtil coordinatesUtil;
	
	private double shiftX, shiftY, scale, panX;

	private Date currentDate;

	private GradientsUtil gradientsUtil;
	
	private double gradientMin, gradientMax;
	
	private String gradientId;
	
	@Inject
	public HorizontalSlicePresenter(DapController dapController, CoordinatesUtil coordinatesUtil,
			GradientsUtil gradientsUtil) {
		this.dapController = dapController;
		this.coordinatesUtil = coordinatesUtil;
		this.gradientsUtil = gradientsUtil;
	}
	
	public void onUpdateHorizontalSliceConfiguration(
			HorizontalCrosssectionConfiguration configuration) {
		if(this.configuration == configuration) {
			refreshView();
		}

		addProfilesToMinimap();
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
		refreshView();
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
		addProfilesToMinimap();
	}
	
	public void onGradientExtended(String gradientId) {
		if (gradientId.equals(this.gradientId)
				&& gradientsUtil.isExtended(gradientId, gradientMin, gradientMax)) {
			GWT.log("Gradient " + gradientId + " extended");
			refreshView();
		}
	}

	private void addProfilesToMinimap() {
		selectionManager.clear();
		
		for (Profile profile : configuration.getPickedProfiles().values()) {
			selectionManager.add(profile);
		}
	}

	private void refreshView() {
		if (!view.canRender()) {
			eventBus.showSimpleError(view.cannotRenderMessages());
			
			return;
		}
		
		view.showLoadingState(true);
		
		final List<String> parameterIds = new ArrayList<>();
		Parameter parameter = null;
		
		for(String height : configuration.getPickedHeights().values()) {
			for(Device device : configuration.getHeightDevicesmap().get(height)) {
				for(String parameterId : device.getParameterIds()) {
					if(configuration.getParameterMap().get(parameterId) != null
							&& configuration.getParameterMap().get(parameterId)
							.getMeasurementTypeName().equals(
									configuration.getPickedParameterName())) {
						parameterIds.add(parameterId);
						parameter = configuration.getParameterMap().get(parameterId);
					}
				}
			}
		}
		
		final String parameterUnit = parameter != null ? parameter.getMeasurementTypeUnit() : "";
		String context = configuration.getDataSelector().equals("0") ? "measurements" : "scenarios";
		dapController.getContext(context, new ContextsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showLoadingState(false);
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processContexts(final List<Context> contexts) {
				if(contexts.size() > 0) {
					dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds,
							new TimelinesCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showLoadingState(false);
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processTimelines(final List<Timeline> timelines) {
							if(!configuration.getDataSelector().equals("0")) {
								for(Iterator<Timeline> i = timelines.iterator(); i.hasNext();) {
									if(!i.next().getScenarioId()
											.equals(configuration.getDataSelector())) {
										i.remove();
									}
								}
							}
							
							List<String> timelineIds = new ArrayList<>();
							
							for(Timeline timeline : timelines) {
								timelineIds.add(timeline.getId());
							}
							
							Date queryDate = configuration.getDataSelector().equals("0")
									? currentDate :
								new Date(currentDate.getTime()
										- configuration.getExperiment().getStart().getTime());
							
							dapController.getLastMeasurements(
									timelineIds, queryDate, new MeasurementsCallback() {
								@Override
								public void onError(ErrorDetails errorDetails) {
									view.showLoadingState(false);
									eventBus.showError(errorDetails);
								}

								@Override
								public void processMeasurements(List<Measurement> measurements) {
									view.showLoadingState(false);
									view.clear();
									
									List<Section> muteSections = new ArrayList<>();
									
									SECTION:
									for(Section section : configuration.getSections().values()) {
										for(Profile profile
												: configuration.getPickedProfiles().values()) {
											if(profile.getSectionId().equals(section.getId())) {
												continue SECTION;
											}
										}
										
										muteSections.add(section);
									}
									
									if(measurements.size() > 0) {
										gradientId = "analysis:" + parameterUnit;
										
										if (gradientsUtil.contains(gradientId)) {
											gradientMin = gradientsUtil.getMinValue(gradientId);
											gradientMax = gradientsUtil.getMaxValue(gradientId);
										}
										
										for(Measurement measurement : measurements) {
											gradientsUtil.updateValues(gradientId,
													measurement.getValue());
										}
										
										view.init();
										drawMuteSections(configuration.getSections().values(),
												muteSections);
										view.drawCrosssection(createLegend(gradientId),
												parameterUnit,
												createDeviceLocationsWithValuesAndColors(
														measurements, timelines, contexts.get(0),
														gradientId));
										
										if (gradientsUtil.isExtended(gradientId, gradientMin,
												gradientMax)) {
											gradientMin = gradientsUtil.getMinValue(gradientId);
											gradientMax = gradientsUtil.getMaxValue(gradientId);
											eventBus.gradientExtended(gradientId);
										}
									} else {
										eventBus.showSimpleError(view.noMeasurementsMessage());
									}
								}
							});
						}
					});
				}
			}
		});
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	private Map<List<List<Double>>, Map<List<Double>, List<Double>>>
			createDeviceLocationsWithValuesAndColors(List<Measurement> measurements,
					List<Timeline> timelines, Context context, String gradientId) {
		Map<List<List<Double>>, Map<List<Double>, List<Double>>> result = new HashMap<>();
		
		for(Profile profile : configuration.getPickedProfiles().values()) {
			Map<List<Double>, Double> temp = new LinkedHashMap<>();
			List<List<Double>> keys = new ArrayList<>();
			Double value = 0.0;
			
			for(Device device : configuration.getProfileDevicesMap().get(profile)) {
				PARAMETER:
				for(Parameter parameter : configuration.getParameterMap().values()) {
					if(device.getParameterIds().contains(parameter.getId())
							&& parameter.getMeasurementTypeName().equals(
									configuration.getPickedParameterName())) {
						for(Timeline timeline : timelines) {
							if(timeline.getContextId().equals(context.getId())
									&& parameter.getTimelineIds().contains(timeline.getId())) {
								for(Measurement measurement : measurements) {
									if(measurement.getTimelineId().equals(timeline.getId())) {
										value = measurement.getValue();
										
										break PARAMETER;
									}
								}
							}
						}
					}
				}
				
				if(device.getPlacement() != null
						&& device.getPlacement().getCoordinates() != null) {
					List<List<Double>> coordinates = new ArrayList<>();
					coordinates.add(device.getPlacement().getCoordinates());
					
					List<List<Double>> projectedCoordinates = coordinatesUtil.projectCoordinates(
							coordinates);
					rotate(projectedCoordinates);
					
					for(List<Double> pointCoordinates : projectedCoordinates) {
						pointCoordinates.set(0, pointCoordinates.get(0) - shiftX);
						pointCoordinates.set(1, pointCoordinates.get(1) - shiftY);
					}
					
					List<List<List<Double>>> toBeScaledAndShiftedCoordinates = new ArrayList<>();
					toBeScaledAndShiftedCoordinates.add(projectedCoordinates);
					scaleAndShift(toBeScaledAndShiftedCoordinates, scale, panX);
					temp.put(toBeScaledAndShiftedCoordinates.get(0).get(0), value);
					keys.add(toBeScaledAndShiftedCoordinates.get(0).get(0));
				}
			}
			
			sort(keys, new Comparator<List<Double>>() {
				@Override
				public int compare(List<Double> o1, List<Double> o2) {
					return -o1.get(1).compareTo(o2.get(1));
				}
			});
			
			Map<List<Double>, List<Double>> locationsWithReadings = new LinkedHashMap<>();
			
			for(List<Double> key : keys) {
				Double finalValue = temp.get(key);
				Color color = gradientsUtil.getColor(gradientId, finalValue);
				List<Double> valueWithColor = new ArrayList<>();
				valueWithColor.add(finalValue);
				valueWithColor.add(new Integer(color.getR()).doubleValue());
				valueWithColor.add(new Integer(color.getG()).doubleValue());
				valueWithColor.add(new Integer(color.getB()).doubleValue());
				locationsWithReadings.put(key, valueWithColor);
			}
			
			Section section = configuration.getSections().get(profile.getSectionId());
			//removing last element which is just there to close the loop
			List<List<Double>> corners = section.getShape().getCoordinates()
					.subList(0, section.getShape().getCoordinates().size() - 1);
			List<List<Double>> projectedCorners = coordinatesUtil.projectCoordinates(corners);
			rotate(projectedCorners);
			
			for(List<Double> pointCoordinates : projectedCorners) {
				pointCoordinates.set(0, pointCoordinates.get(0) - shiftX);
				pointCoordinates.set(1, pointCoordinates.get(1) - shiftY);
			}
			
			List<List<List<Double>>> scaledAndShiftedCoordinates = new ArrayList<>();
			scaledAndShiftedCoordinates.add(projectedCorners);
			scaleAndShift(scaledAndShiftedCoordinates, scale, panX);
			List<List<Double>> scaled = scaledAndShiftedCoordinates.get(0);
			sort(scaled, new Comparator<List<Double>>() {
				@Override
				public int compare(List<Double> o1, List<Double> o2) {
					return -o1.get(1).compareTo(o2.get(1));
				}
			});
			
			if(scaled.size() > 3) {
				if(scaled.get(0).get(0) > scaled.get(1).get(0)) {
					scaled.add(0, scaled.remove(1));
				}
				
				if(scaled.get(2).get(0) < scaled.get(3).get(0)) {
					scaled.add(2, scaled.remove(3));
				}
			}
			
			result.put(scaled , locationsWithReadings);
		}
		
		return result;
	}

	private void drawMuteSections(Collection<Section> allSections, List<Section> muteSections) {
		List<List<List<Double>>> coordinates = new ArrayList<>();
		double	maxX = Double.MIN_VALUE,
				maxY = Double.MIN_VALUE;
		shiftX = Double.MAX_VALUE;
		shiftY = Double.MAX_VALUE;
		
		for(Section section : muteSections) {
			if(section.getShape() != null) {
				List<List<Double>> projected = coordinatesUtil.projectCoordinates(
						section.getShape().getCoordinates());
				rotate(projected);
				coordinates.add(projected);
				
				for(List<Double> point : projected) {
					if(point.get(0) > maxX) {
						maxX = point.get(0);
					}
					
					if(point.get(0) < shiftX) {
						shiftX = point.get(0);
					}
					
					if(point.get(1) > maxY) {
						maxY = point.get(1);
					}
					
					if(point.get(1) < shiftY) {
						shiftY = point.get(1);
					}
				}
			}
		}
		
		for(List<List<Double>> sectionCoordinates : coordinates) {
			for(List<Double> pointCoordinates : sectionCoordinates) {
				pointCoordinates.set(0, pointCoordinates.get(0) - shiftX);
				pointCoordinates.set(1, pointCoordinates.get(1) - shiftY);
			}
		}
		
		panX = 200;
		scale = computeScale(coordinates, panX, view.getHeight(), view.getWidth());
		scaleAndShift(coordinates, scale, panX);
		view.drawScale(scale, panX);
		view.drawMuteSections(coordinates);
	}

	private double computeScale(List<List<List<Double>>> coordinates, double panX, double height,
			double width) {
		double 	minY = Double.MAX_VALUE,
				maxY = Double.MIN_VALUE,
				minX = Double.MAX_VALUE,
				maxX = Double.MIN_VALUE;
		
		for(List<List<Double>> sectionCoordinates : coordinates) {
			for(List<Double> point : sectionCoordinates) {
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
		
		double sectionsHeight = abs(minY) + abs(maxY);
		double sectionsWidth = abs(minX) + abs(maxX);
		
		return min(height / sectionsHeight, (width - panX) / sectionsWidth);
	}

	private void scaleAndShift(List<List<List<Double>>> coordinates, double scale, double panX) {
		for(List<List<Double>> sectionCoordinates : coordinates) {
			for(List<Double> point : sectionCoordinates) {
				point.set(0, point.get(0) * scale + panX);
				point.set(1, point.get(1) * scale);
			}
		}
	}

	private void rotate(List<List<Double>> points) {
		for(List<Double> point : points) {
			double newX = point.get(0) * cos(PI / 2) - point.get(1) * sin(PI / 2);
			double newY = point.get(0) * sin(PI / 2) + point.get(1) * cos(PI / 2);
			point.set(0, newX);
			point.set(1, newY);
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
}