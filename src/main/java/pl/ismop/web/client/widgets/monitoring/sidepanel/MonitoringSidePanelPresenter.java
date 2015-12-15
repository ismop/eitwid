package pl.ismop.web.client.widgets.monitoring.sidepanel;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.*;
import pl.ismop.web.client.dap.context.Context;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.timeline.Timeline;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.chart.ChartPresenter;
import pl.ismop.web.client.widgets.common.chart.ChartSeries;
import pl.ismop.web.client.widgets.monitoring.sidepanel.IMonitoringSidePanel.IMonitoringSidePanelPresenter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Presenter(view = MonitoringSidePanelView.class, multiple = true)
public class MonitoringSidePanelPresenter extends BasePresenter<IMonitoringSidePanel, MainEventBus> implements IMonitoringSidePanelPresenter {
	private DapController dapController;
	private Levee selectedLevee;
	private ChartPresenter chartPresenter;

	@Inject
	public MonitoringSidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onLeveeNavigatorReady() {
		if(selectedLevee != null) {
			eventBus.leveeSelected(selectedLevee);
		}
	}
	
	public void onShowProfileMetadata(Profile profile, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getTypeLabel(), view.getProfileTypeLabel());
			view.addMetadata(view.getInternalIdLabel(), profile.getId());
		}
	}
	
	public void onShowSectionMetadata(Section section, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getTypeLabel(), view.getSectionTypeLabel());
			view.addMetadata(view.getInternalIdLabel(), section.getId());
		}
	}
	
	public void onShowDeviceMetadata(Device device, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getTypeLabel(), view.getDeviceTypeLabel());
			view.addMetadata(view.getInternalIdLabel(), device.getId());
			view.addMetadata(view.getNameLabel(), device.getCustomId());
		}
	}
	
	public void onShowDeviceAggregateMetadata(DeviceAggregate deviceAggregate, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getTypeLabel(), view.getDeviceAggregateTypeLabel());
			view.addMetadata(view.getInternalIdLabel(), deviceAggregate.getId());
			view.addMetadata(view.getAggregateContentsLabel(), deviceAggregate.getCustomId());
		}
	}
	
	public void onDeviceSelected(Device device, boolean selected) {
		if(selected) {
			addChartSeries(device);
		} else {
			if(chartPresenter != null) {
				chartPresenter.removeChartSeriesForDevice(device);
				
				if(chartPresenter.getSeriesCount() == 0) {
					view.showChartButtons(false);
				}
			}
		}
	}

	public void reset() {
		view.showLeveeName(false);
		view.showLeveeList(false);
		view.showLeveeProgress(true);
		loadLevees();
	}

	@Override
	public void handleShowFibreClick() {
		eventBus.showFibrePanel(selectedLevee);
	}

	@Override
	public void handleShowWeatherClick() {
		eventBus.showWeatherPanel();
	}

	@Override
	public void onExpandChart() {
		eventBus.showExpandedReadings(selectedLevee, chartPresenter.getSeries());
	}

	@Override
	public void onClearChart() {
		eventBus.clearSelection();
	}

	private void addChartSeries(final Device device) {
		if(chartPresenter == null) {
			chartPresenter = eventBus.addHandler(ChartPresenter.class);
			chartPresenter.setHeight(view.getChartHeight());
			view.setChart(chartPresenter.getView());
		}
		
		chartPresenter.setLoadingState(true);
		dapController.getParameters(device.getId(), new ParametersCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
				chartPresenter.setLoadingState(false);
			}
			
			@Override
			public void processParameters(final List<Parameter> parameters) {
				dapController.getContext("measurements", new ContextsCallback() {
					@Override
					public void onError(ErrorDetails errorDetails) {
						eventBus.showError(errorDetails);
						chartPresenter.setLoadingState(false);
					}
					
					@Override
					public void processContexts(List<Context> contexts) {
						if(contexts.size() > 0) {
							List<String> parameterIds = new ArrayList<>();
							
							for(Parameter parameter : parameters) {
								parameterIds.add(parameter.getId());
							}
							
							dapController.getTimelinesForParameterIds(contexts.get(0).getId(), parameterIds, new TimelinesCallback() {
								@Override
								public void onError(ErrorDetails errorDetails) {
									eventBus.showError(errorDetails);
									chartPresenter.setLoadingState(false);
								}
								
								@Override
								public void processTimelines(final List<Timeline> timelines) {
									List<String> timelineIds = new ArrayList<>();
									
									for(Timeline timeline : timelines) {
										timelineIds.add(timeline.getId());
									}
									
									dapController.getMeasurementsForTimelineIdsWithQuantity(timelineIds, 1000, new MeasurementsCallback() {
										@Override
										public void onError(ErrorDetails errorDetails) {
											eventBus.showError(errorDetails);
											chartPresenter.setLoadingState(false);
										}
										
										@Override
										public void processMeasurements(List<Measurement> measurements) {
											chartPresenter.setLoadingState(false);
											
											for(Parameter parameter : parameters) {
												
												String timelineId = null;
												
												for(Timeline timeline : timelines) {
													if(parameter.getTimelineIds().contains(timeline.getId())) {
														timelineId = timeline.getId();
														
														break;
													}
												}
												
												if(timelineId != null) {
													List<Measurement> parameterMeasurements = new ArrayList<>();
													
													for(Measurement measurement : measurements) {
														if(measurement.getTimelineId().equals(timelineId)) {
															parameterMeasurements.add(measurement);
														}
													}
													
													if(parameterMeasurements.size() > 0) {
														ChartSeries series = new ChartSeries();
														series.setName(device.getCustomId() + " (" + parameter.getMeasurementTypeName() + ")");
														series.setDeviceId(parameter.getDeviceId());
														series.setParameterId(parameter.getId());
														series.setLabel(parameter.getMeasurementTypeName());
														series.setUnit(parameter.getMeasurementTypeUnit());
														
														Number[][] values = new Number[parameterMeasurements.size()][2];
														int index = 0;
														
														for(Measurement measurement : parameterMeasurements) {
															values[index][0] = measurement.getTimestamp().getTime();
															values[index][1] = measurement.getValue();
															index++;
														}
														
														series.setValues(values);
														chartPresenter.addChartSeries(series);
														view.showChartButtons(true);
													} else {
														eventBus.showSimpleError(view.getNoMeasurementsForDeviceMessage());
													}
												}
											}
										}
									});
								}
							});
						}
					}
				});
			}
		});
	}

	private void loadLevees() {
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showLeveeProgress(false);
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				view.showLeveeProgress(false);
				
				if(levees.size() > 0) {
					if(levees.size() == 1) {
						view.setLeveeName(levees.get(0).getName());
						view.showLeveeName(true);
					} else {
						Collections.sort(levees, new Comparator<Levee>() {
							@Override
							public int compare(Levee o1, Levee o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});
						
						for(Levee levee : levees) {
							view.addLeveeOption(levee.getId(), levee.getName());
						}
					}
					
					selectedLevee = levees.get(0);
					eventBus.leveeSelected(selectedLevee);
				} else {
					view.showNoLeveesMessage();
				}
			}
		});
	}
}