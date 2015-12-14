package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.util.CoordinatesUtil;
import pl.ismop.web.client.widgets.analysis.verticalslice.IVerticalSliceView.IVerticalSlicePresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = VerticalSliceView.class, multiple = true)
public class VerticalSlicePresenter extends BasePresenter<IVerticalSliceView, MainEventBus> implements IVerticalSlicePresenter, 
		IPanelContent<IVerticalSliceView, MainEventBus> {
	private DapController dapController;
	
	private VerticalCrosssectionConfiguration configuration;

	private Date currentDate;

	private CoordinatesUtil coordinatesUtil;

	private ISelectionManager selectionManager;

	@Inject
	public VerticalSlicePresenter(DapController dapController, CoordinatesUtil coordinatesUtil) {
		this.dapController = dapController;
		this.coordinatesUtil = coordinatesUtil;
	}
	
	public void onUpdateVerticalSliceConfiguration(VerticalCrosssectionConfiguration configuration) {
		if(this.configuration == configuration) {
			refreshView();
		}
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
		selectionManager.showProfile(configuration.getPickedProfile());
	}

	@Override
	public void destroy() {
		
	}

	private void refreshView() {
		view.showLoadingState(true);
		
		final List<String> parameterIds = new ArrayList<>();
		Parameter parameter = null;
		
		for(Device device : configuration.getProfileDevicesMap().get(configuration.getPickedProfile())) {
			for(String parameterId : device.getParameterIds()) {
				if(configuration.getParameterMap().get(parameterId) != null
						&& configuration.getParameterMap().get(parameterId).getMeasurementTypeName().equals(configuration.getPickedParameterName())) {
					parameterIds.add(parameterId);
					parameter = configuration.getParameterMap().get(parameterId);
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
			public void processContexts(List<Context> contexts) {
				if(contexts.size() > 0) {
					dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds, new TimelinesCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showLoadingState(false);
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processTimelines(final List<Timeline> timelines) {
							if(!configuration.getDataSelector().equals("0")) {
								for(Iterator<Timeline> i = timelines.iterator(); i.hasNext();) {
									if(!i.next().getScenarioId().equals(configuration.getDataSelector())) {
										i.remove();
									}
								}
							}
							
							List<String> timelineIds = new ArrayList<>();
							
							for(Timeline timeline : timelines) {
								timelineIds.add(timeline.getId());
							}
							
							Date queryDate = configuration.getDataSelector().equals("0") ? currentDate :
								new Date(currentDate.getTime() - configuration.getExperiment().getStart().getTime());
							
							dapController.getLastMeasurements(timelineIds, queryDate, new MeasurementsCallback() {
								@Override
								public void onError(ErrorDetails errorDetails) {
									view.showLoadingState(false);
									eventBus.showError(errorDetails);
								}
								
								@Override
								public void processMeasurements(List<Measurement> measurements) {
									view.showLoadingState(false);
									view.clear();
									
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
										
										//TODO: come up with a better way of telling where the water is
										boolean leftBank = true;
										
										if(configuration.getPickedProfile().getShape().getCoordinates().get(0).get(0) > 19.676851838778) {
											leftBank = false;
										}
										
										view.init();
										view.drawCrosssection(parameterUnit, minValue, maxValue, leftBank,
												calculateProfileAndDevicePositionsWithValues(measurements, timelines, configuration.getPickedProfile()));
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
	
	private Map<Double, Double> calculateProfileAndDevicePositionsWithValues(List<Measurement> measurements, List<Timeline> timelines, Profile profile) {
		Map<Double, Double> result = new LinkedHashMap<>();
		List<List<Double>> toBeProjected = new ArrayList<>();
		toBeProjected.addAll(profile.getShape().getCoordinates());
		
		List<Device> devices = configuration.getProfileDevicesMap().get(profile);
		Collections.sort(devices, new Comparator<Device>() {
			@Override
			public int compare(Device o1, Device o2) {
				return o1.getPlacement().getCoordinates().get(0).compareTo(o2.getPlacement().getCoordinates().get(0));
			}
		});
		
		for(Device device : devices) {
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
		
		for(List<Double> point : projected) {
			point.set(0, point.get(0) - minX);
		}
		
		for(int i = 0; i < projected.size(); i++) {
			if(i == 0) {
				//left profile coordinate
				result.put(projected.get(i).get(0), getDeviceValue(devices.get(0), measurements, timelines));
			} else if(i == projected.size() - 1) {
				//right profile coordinate 
				result.put(projected.get(i).get(0), getDeviceValue(devices.get(devices.size() - 1), measurements, timelines));
			} else {
				//device coordinates
				result.put(projected.get(i).get(0), getDeviceValue(devices.get(i - 1), measurements, timelines));
			}
		}
		
		return result;
	}

	private Double getDeviceValue(Device device, List<Measurement> measurements, List<Timeline> timelines) {
		for(Timeline timeline : timelines) {
			if(device.getParameterIds().contains(timeline.getParameterId())) {
				for(Measurement measurement : measurements) {
					if(measurement.getTimelineId().equals(timeline.getId())) {
						return measurement.getValue();
					}
				}
			}
		}
		
		return 0.0;
	}
}