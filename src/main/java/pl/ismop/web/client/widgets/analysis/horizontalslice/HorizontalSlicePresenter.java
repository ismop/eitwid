package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
		
		for(String height : configuration.getPickedHeights().values()) {
			for(Device device : configuration.getHeightDevicesmap().get(height)) {
				for(String parameterId : device.getParameterIds()) {
					if(configuration.getParameterMap().get(parameterId) != null
							&& configuration.getParameterMap().get(parameterId).getMeasurementTypeName().equals(configuration.getPickedParameterName())) {
						parameterIds.add(parameterId);
					}
				}
			}
		}
		
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
									
									view.drawCrosssection();
									drawMuteSections(configuration.getSections().values(), muteSections);
								}
							});
						}
					});
				}
			}
		});
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
		
		view.drawMuteSections(coordinates);
	}
}