package pl.ismop.web.client.widgets.old.sidepanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Series.Type;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.DevicesCallback;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.PolygonShape;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.internal.ExperimentPlanBean;
import pl.ismop.web.client.internal.InternalExperimentController;
import pl.ismop.web.client.internal.InternalExperimentController.ExperimentPlansCallback;
import pl.ismop.web.client.widgets.old.plot.PlotPresenter;
import pl.ismop.web.client.widgets.old.section.SectionPresenter;
import pl.ismop.web.client.widgets.old.sidepanel.ISidePanelView.ISidePanelPresenter;
import pl.ismop.web.client.widgets.old.summary.LeveeSummaryPresenter;

@Presenter(view = SidePanelView.class)
public class SidePanelPresenter extends BasePresenter<ISidePanelView, MainEventBus> implements ISidePanelPresenter {
	private DapController dapController;
	private InternalExperimentController internalExperimentController;
	private LeveeSummaryPresenter leveeSummaryPresenter;
	private SectionPresenter sectionPresenter;
	private Map<String, Section> sections;
	private Map<String, Profile> profiles;
	private Map<String, Device> devices;
	private PlotPresenter plotPresenter;

	@Inject
	public SidePanelPresenter(DapController dapController, InternalExperimentController internalExperimentController) {
		this.dapController = dapController;
		this.internalExperimentController = internalExperimentController;
		sections = new HashMap<>();
		profiles = new HashMap<>();
		devices = new HashMap<>();
	}
	
	public void onStart() {
//		eventBus.setSidePanel(view);
		view.setLeveeBusyState(true);
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.setLeveeBusyState(false);
				Window.alert("Error: " + errorDetails.getMessage());
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				view.setLeveeBusyState(false);
				
				if(levees.size() > 0) {
					view.showLeveeList(true);
					
					for(Levee levee : levees) {
						view.addLeveeValue(levee.getId(), levee.getName());
					}
					
					loadLeveeStatus(levees.get(0));
//					eventBus.drawGoogleMap("mainPanel", levees.get(0).getId());
					loadSections(levees.get(0).getId());
				} else {
					view.showNoLeveesLabel(true);
				}
			}
		});
		internalExperimentController.getExperimentPlans(new ExperimentPlansCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert(message);
			}
			
			@Override
			public void processExperimentPlans(List<ExperimentPlanBean> experimentPlans) {
				if(experimentPlans.size() > 0) {
					ExperimentPlanBean experimentPlan = experimentPlans.get(0);
					view.setExperimentPlanName(experimentPlan.getName());
					view.setExperimentPlanStartDate(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(experimentPlan.getStartDate()));
					view.setExperimentPlanMargin("+/- " + String.valueOf(experimentPlan.getMargin()) + " m");
					
					String[] entries = experimentPlan.getWave().split(";");
					Number[][] values = new Number[entries.length][2];
					Number[][] range = new Number[entries.length][3];
					int index = 0;
					
					for(String entry : entries) {
						String[] bothValues = entry.split(",");
						float time = Float.parseFloat(bothValues[0]);
						float height = Float.parseFloat(bothValues[1]);
						values[index][0] = time;
						values[index][1] = height;
						
						range[index][0] = time;
						range[index][1] = height - experimentPlan.getMargin();
						range[index][2] = height + experimentPlan.getMargin();
						
						index++;
					}
					
					Chart chart = new Chart()
							.setChartTitle(new ChartTitle().setText("Przebieg fali"));
					chart.getYAxis(0)
							.setAxisTitle(new AxisTitle().setText("Wysokość [m]"));
					chart.addSeries(chart.createSeries()
							.setName("Odchyłka [m]")
							.setType(Type.AREA_RANGE)
							.setYAxis(0)
							.setPoints(range));
					chart.addSeries(chart.createSeries()
							.setName("Czas [h]")
							.setType(Type.LINE)
							.setYAxis(0)
							.setPoints(values));
					view.setExperimentPlanChart(chart);
				} else {
					view.getNoExperimentPlansVisibility().setVisible(true);
				}
			}
		});
	}
	
	public void onSectionSelectedOnMap(String sectionId) {
		loadSectionStatus(sections.get(sectionId), false);
		view.setSelectedSection(sectionId);
	}
	
	@Override
	public void onSectionChanged(String sectionId) {
		loadSectionStatus(sections.get(sectionId), true);
	}

	@Override
	public void onProfileChanged(String profileId) {
		if(profileId.isEmpty()) {
			view.showDevicePanel(false);
			view.setDeviceBusyState(false);
			view.showPlotContainer(false);
			view.showDeviceList(false);
//			eventBus.removeProfileAggregates();
		} else {
			view.showDevicePanel(true);
			view.setDeviceBusyState(true);
			dapController.getDevicesRecursively(profileId, new DevicesCallback() {
				@Override
				public void onError(ErrorDetails errorDetails) {
					view.setDeviceBusyState(false);
					Window.alert("Error: " + errorDetails.getMessage());
				}
				
				@Override
				public void processDevices(List<Device> devices) {
					SidePanelPresenter.this.devices.clear();
					
					for(Device device : devices) {
						SidePanelPresenter.this.devices.put(device.getId(), device);
					}
					
					view.setDeviceBusyState(false);
					
					if(devices.size() > 0) {
						view.showDeviceList(true);
						view.clearDeviceValues();
						
						for(Device device : devices) {
							view.addDeviceValue(device.getId(), device.getCustomId());
						}
					} else {
						view.showNoDevicesLabel(true);
					}
				}
			});
//			eventBus.markAndCompleteProfile(profileId);
		}
	}

	@Override
	public void onDeviceChanged(List<String> deviceIds) {
		if(deviceIds.size() == 0) {
			view.showPlotContainer(false);
		} else {
			view.showPlotContainer(true);
			
			if(plotPresenter == null) {
				plotPresenter = eventBus.addHandler(PlotPresenter.class);
				view.setPlotView(plotPresenter.getView());
			}
			
			Map<String, Device> drawnDevices = new HashMap<>();
			
			for(String deviceId : deviceIds) {
				drawnDevices.put(deviceId, devices.get(deviceId));
			}
			
			plotPresenter.drawMeasurements(drawnDevices);
		}
	}
	
	public void onProfilePicked(String profileId) {
		view.setSelectedProfile(profileId);
		onProfileChanged(profileId);
	}
	
	public void onAggregatePicked(String aggregateId) {
		dapController.getDevicesRecursivelyForAggregate(aggregateId, new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				Window.alert("Error: " + errorDetails.getMessage());
			}
			
			@Override
			public void processDevices(List<Device> devices) {
				List<String> deviceIds = new ArrayList<>();
				
				for(Device device : devices) {
					deviceIds.add(device.getId());
				}
				
				view.setSelectedDevices(deviceIds);
				onDeviceChanged(deviceIds);
			}
		});
	}
	
	@Override
	public void onAddExperiment() {
//		eventBus.showExperiment();
	}

	private void loadLeveeStatus(Levee levee) {
		if(leveeSummaryPresenter != null) {
			leveeSummaryPresenter.stopUpdate();
			view.removeSummaryView();
			eventBus.removeHandler(leveeSummaryPresenter);
			leveeSummaryPresenter = null;
		}
		
		leveeSummaryPresenter = eventBus.addHandler(LeveeSummaryPresenter.class);
		view.setLeveeSummary(leveeSummaryPresenter.getView());
		leveeSummaryPresenter.setLevee(levee);
	}
	
	private void loadSections(String leveeId) {
		view.showSectionPanel(true);
		view.setSectionBusyState(true);
		dapController.getSections(leveeId, new SectionsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.setSectionBusyState(false);
				Window.alert("Error: " + errorDetails.getMessage());
			}
			
			@Override
			public void processSections(List<Section> sections) {
				SidePanelPresenter.this.sections.clear();
				view.setSectionBusyState(false);
				
				if(sections.size() > 0) {
					view.showSectionList(true);
					view.addSectionValue("", view.getPickSectionLabel());
					
					for(Section section : sections) {
						view.addSectionValue(section.getId(), section.getId());
						SidePanelPresenter.this.sections.put(section.getId(), section);
					}
				} else {
					view.showNoSectionsLabel(true);
				}
			}
		});
	}

	private void loadSectionStatus(Section section, boolean fireEvent) {
		if(sectionPresenter != null) {
			sectionPresenter.stopUpdate();
			view.removeSectionView();
			eventBus.removeHandler(sectionPresenter);
			sectionPresenter = null;
//			eventBus.removeProfileAggregates();
		}
		
		if(section != null) {
			sectionPresenter = eventBus.addHandler(SectionPresenter.class);
			view.setSectionView(sectionPresenter.getView());
			sectionPresenter.setSection(section);
			
			if(fireEvent) {
//				eventBus.zoomToAndSelectSection(section.getId());
			}
			
			loadProfiles(section.getId());
		} else {
			profiles.clear();
			view.clearProfileValues();
			view.showProfilePanel(false);
			view.showDevicePanel(false);
//			eventBus.deselectSection();
//			eventBus.removeProfileAggregates();
//			eventBus.removeProfiles();
//			eventBus.zoomToLevee(view.getSelectedLeveeId());
		}
	}

	private void drawProfileLayer(List<Profile> profiles) {
		Map<String, PolygonShape> profileShapes = new HashMap<>();
		
		for(Profile profile : profiles) {
			if(profile.getShape() != null) {
				profileShapes.put(profile.getId(), profile.getShape());
			}
		}
		
//		eventBus.drawProfiles(profileShapes);
	}

	private void loadProfiles(String sectionId) {
		profiles.clear();
		view.clearProfileValues();
		view.showProfilePanel(true);
		view.setProfileBusyState(true);
		view.showDevicePanel(false);
		view.showNoDevicesLabel(false);
		view.showPlotContainer(false);
		view.showProfileList(false);
		dapController.getProfiles(sectionId, new ProfilesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				Window.alert("Error: " + errorDetails.getMessage());
			}
			
			@Override
			public void processProfiles(List<Profile> profiles) {
				view.setProfileBusyState(false);
				
				if(profiles.size() > 0) {
					view.showProfileList(true);
					view.addProfileValue("", view.getPickProfileLabel());
					
					for(Profile profile : profiles) {
						view.addProfileValue(profile.getId(), profile.getId());
						SidePanelPresenter.this.profiles.put(profile.getId(), profile);
						drawProfileLayer(profiles);
					}
				} else {
					view.showNoProfilesLabel(true);
				}
			}
		});
	}
}