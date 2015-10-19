package pl.ismop.web.client.widgets.analysis.horizontalslice;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.gwt.core.shared.GWT;
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
import pl.ismop.web.client.widgets.analysis.horizontalslice.IHorizontalSliceView.IHorizontalSlicePresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = HorizontalSliceView.class, multiple = true)
public class HorizontalSlicePresenter extends BasePresenter<IHorizontalSliceView, MainEventBus> implements IHorizontalSlicePresenter,
		IPanelContent<IHorizontalSliceView, MainEventBus> {
	private HorizontalCrosssectionConfiguration configuration;
	
	private ISelectionManager selectionManager;

	private DapController dapController;

	private CoordinatesUtil coordinatesUtil;
	
	@Inject
	public HorizontalSlicePresenter(DapController dapController, CoordinatesUtil coordinatesUtil) {
		this.dapController = dapController;
		this.coordinatesUtil = coordinatesUtil;
	}
	
	public void onUpdateHorizontalSliceConfiguration(HorizontalCrosssectionConfiguration configuration) {
		if(this.configuration == configuration) {
			//TODO
		}
	}
	
	public void onDateChanged(Date selectedDate) {
		processDateChange(selectedDate);
	}

	@Override
	public void setSelectedExperiment(Experiment experiment) {
		//not needed for now
	}

	@Override
	public void setSelectedDate(Date date) {
		processDateChange(date);
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
	}

	private void processDateChange(final Date date) {
		view.showLoadingState(true);
		
		final List<String> parameterIds = new ArrayList<>();
		Parameter parameter = null;
		
		for(String height : configuration.getPickedHeights().values()) {
			for(Device device : configuration.getHeightDevicesmap().get(height)) {
				for(String parameterId : device.getParameterIds()) {
					if(configuration.getParameterMap().get(parameterId) != null
							&& configuration.getParameterMap().get(parameterId).getMeasurementTypeName().equals(configuration.getPickedParameterName())) {
						parameterIds.add(parameterId);
						parameter = configuration.getParameterMap().get(parameterId);
					}
				}
			}
		}
		
		final String parameterUnit = parameter != null ? parameter.getMeasurementTypeUnit() : "";
		dapController.getContext("measurements", new ContextsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showLoadingState(false);
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processContexts(List<Context> contexts) {
				if(contexts.size() > 0) {
					dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds, new TimelinesCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showLoadingState(false);
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processTimelines(List<Timeline> timelines) {
							List<String> timelineIds = new ArrayList<>();
							
							for(Timeline timeline : timelines) {
								timelineIds.add(timeline.getId());
							}
							
							dapController.getLastMeasurements(timelineIds, date, new MeasurementsCallback() {
								@Override
								public void onError(ErrorDetails errorDetails) {
									view.showLoadingState(false);
									eventBus.showError(errorDetails);
								}

								@Override
								public void processMeasurements(List<Measurement> measurements) {
									view.showLoadingState(false);
									
									List<Section> muteSections = new ArrayList<>();
									
									SECTION:
									for(Section section : configuration.getSections().values()) {
										for(Profile profile : configuration.getPickedProfiles().values()) {
											if(profile.getSectionId().equals(section.getId())) {
												continue SECTION;
											}
										}
										
										muteSections.add(section);
									}
									
									if(measurements.size() > 0) {
										double minValue = Double.MAX_VALUE, maxValue = Double.MIN_VALUE;
										
										for(Measurement measurement : measurements) {
											if(measurement.getValue() > maxValue) {
												maxValue = measurement.getValue();
											}
											
											if(measurement.getValue() < minValue) {
												minValue = measurement.getValue();
											}
										}
										
										view.drawCrosssection(parameterUnit, minValue, maxValue);
										drawMuteSections(configuration.getSections().values(), muteSections);
									} else {
										view.showNoMeasurementsMessage();
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

	private void drawMuteSections(Collection<Section> allSections, List<Section> muteSections) {
		List<List<List<Double>>> coordinates = new ArrayList<>();
		double 	minX = Double.MAX_VALUE,
				minY = Double.MAX_VALUE,
				maxX = Double.MIN_VALUE,
				maxY = Double.MIN_VALUE;
		
		for(Section section : muteSections) {
			if(section.getShape() != null) {
				List<List<Double>> projected = coordinatesUtil.projectCoordinates(section.getShape().getCoordinates());
				rotate(projected);
				coordinates.add(projected);
				
				for(List<Double> point : projected) {
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
		}
		
		for(List<List<Double>> sectionCoordinates : coordinates) {
			for(List<Double> pointCoordinates : sectionCoordinates) {
				pointCoordinates.set(0, pointCoordinates.get(0) - minX);
				pointCoordinates.set(1, pointCoordinates.get(1) - minY);
			}
		}
		
		double panX = 200;
		double scale = computeScale(coordinates, panX, view.getHeight(), view.getWidth());
		scaleAndShift(coordinates, scale, panX);
		view.drawScale(scale, panX);
		view.drawMuteSections(coordinates);
	}

	private double computeScale(List<List<List<Double>>> coordinates, double panX, double height, double width) {
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
}