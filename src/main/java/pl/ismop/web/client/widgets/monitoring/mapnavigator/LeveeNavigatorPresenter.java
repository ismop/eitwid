package pl.ismop.web.client.widgets.monitoring.mapnavigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.core.shared.GWT;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.DeviceAggregatesCallback;
import pl.ismop.web.client.dap.DapController.DevicesCallback;
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.common.profile.SideProfilePresenter;
import pl.ismop.web.client.widgets.monitoring.mapnavigator.ILeveeNavigatorView.ILeveeNavigatorPresenter;

@Presenter(view = LeveeNavigatorView.class, multiple = true)
public class LeveeNavigatorPresenter extends BasePresenter<ILeveeNavigatorView, MainEventBus> implements ILeveeNavigatorPresenter {
	private MapPresenter mapPresenter;
	private Levee displayedLevee;
	private DapController dapController;
	private List<Device> displayedDevices;
	private List<DeviceAggregate> displayedDeviceAggregations;
	private Map<String, Device> selectedDevices;
	private Section selectedSection;
	private Map<String, DeviceAggregate> selectedDeviceAggregates;
	private SideProfilePresenter profilePresenter;

	@Inject
	public LeveeNavigatorPresenter(DapController dapController) {
		this.dapController = dapController;
		displayedDevices = new ArrayList<>();
		displayedDeviceAggregations = new ArrayList<>();
		selectedDevices = new HashMap<>();
		selectedDeviceAggregates = new HashMap<>();
	}
	
	@Override
	public void bind() {
		eventBus.leveeNavigatorReady();
	}
	
	public void onLeveeSelected(Levee levee) {
		if(displayedLevee == null || !displayedLevee.getId().equals(levee.getId())) {
			displayedLevee = levee;
			
			if(mapPresenter == null) {
				mapPresenter = eventBus.addHandler(MapPresenter.class);
				mapPresenter.addHoverListeners();
				mapPresenter.addClickListeners();
			}
			
			mapPresenter.reset(false);
			view.showMap(false);
			view.showProgress(true);
			dapController.getSections(displayedLevee.getId(), new SectionsCallback() {
				@Override
				public void onError(ErrorDetails errorDetails) {
					view.showProgress(false);
					eventBus.showError(errorDetails);
				}
				
				@Override
				public void processSections(final List<Section> sections) {
					List<String> sectionIds = collectSectionIds(sections);
					dapController.getProfiles(sectionIds, new ProfilesCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showProgress(false);
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processProfiles(List<Profile> profiles) {
							view.showProgress(false);
							view.showMap(true);
							view.setMap(mapPresenter.getView());
							
							for(Section section : sections) {
								mapPresenter.addSection(section);
							}
							
							for(Profile profile : profiles) {
								mapPresenter.addProfile(profile);
							}
						}
					});
				}
			});
		}
	}
	
	public void onProfileClicked(Profile profile) {
		if(profilePresenter == null) {
			profilePresenter = eventBus.addHandler(SideProfilePresenter.class);
			view.showMap(false);
			view.showProfile(true);
			profilePresenter.setWidthAndHeight(view.getProfileContainerWidth(), view.getProfileContainerHeight());
			view.setProfile(profilePresenter.getView());
		}
		
		profilePresenter.setProfileNameAndSensors("profile", new ArrayList<Sensor>());
	}
	
	public void onSectionClicked(final Section section) {
		if(selectedSection == section) {
			return;
		}
		
		if(selectedSection != null) {
			mapPresenter.removeAction(selectedSection.getId());
		}
		
		selectedSection = section;
		removeDevicesAndAggregates();
		mapPresenter.zoomOnSection(section);
		mapPresenter.addAction(selectedSection.getId(), view.getZoomOutLabel());
		//TODO(DH): add spinner somewhere
		dapController.getDevicesForSectionAndType(section.getId(), "fiber_optic_node",new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processDevices(List<Device> devices) {
				displayedDevices.addAll(devices);
				
				for(Device device : devices) {
					mapPresenter.addDevice(device);
					
					if(selectedDevices.containsKey(device.getId())) {
						mapPresenter.selectDevice(device, true);
						selectedDevices.put(device.getId(), device);
					}
				}
			}
		});
		dapController.getProfiles(section.getId(), new ProfilesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processProfiles(List<Profile> profiles) {
				dapController.getDeviceAggregations(collectProfileIds(profiles), new DeviceAggregatesCallback() {
					@Override
					public void onError(ErrorDetails errorDetails) {
						eventBus.showError(errorDetails);
					}
					
					@Override
					public void processDeviceAggregations(List<DeviceAggregate> deviceAggreagates) {
						displayedDeviceAggregations.addAll(deviceAggreagates);
						
						for(DeviceAggregate deviceAggregate : deviceAggreagates) {
							mapPresenter.addDeviceAggregate(deviceAggregate);
							
							if(selectedDeviceAggregates.containsKey(deviceAggregate.getId())) {
								mapPresenter.selectDeviceAggregate(deviceAggregate, true);
								selectedDeviceAggregates.put(deviceAggregate.getId(), deviceAggregate);
							}
						}
					}
				});
			}
		});
	}

	public void onDeviceClicked(Device device) {
		if(selectedDevices.containsKey(device.getId())) {
			mapPresenter.selectDevice(device, false);
			selectedDevices.remove(device.getId());
			eventBus.deviceSelected(device, false);
		} else {
			mapPresenter.selectDevice(device, true);
			selectedDevices.put(device.getId(), device);
			eventBus.deviceSelected(device, true);
		}
	}
	
	public void onDeviceAggregateClicked(final DeviceAggregate deviceAggregate) {
		//TODO(DH): add spinner somewhere
		dapController.getDevicesRecursivelyForAggregate(deviceAggregate.getId(), new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processDevices(List<Device> devices) {
				if(selectedDeviceAggregates.containsKey(deviceAggregate.getId())) {
					mapPresenter.selectDeviceAggregate(deviceAggregate, false);
					selectedDeviceAggregates.remove(deviceAggregate.getId());
					
					for(Device device : devices) {
						eventBus.deviceSelected(device, false);
					}
				} else {
					mapPresenter.selectDeviceAggregate(deviceAggregate, true);
					selectedDeviceAggregates.put(deviceAggregate.getId(), deviceAggregate);
					
					for(Device device : devices) {
						eventBus.deviceSelected(device, true);
					}
				}
			}
		});
	}
	
	public void onZoomOut(String sectionId) {
		removeDevicesAndAggregates();
		mapPresenter.zoomToAllSections();
		mapPresenter.removeAction(sectionId);
		selectedSection = null;
	}

	private List<String> collectProfileIds(List<Profile> profiles) {
		List<String> result = new ArrayList<>();
		
		for(Profile profile : profiles) {
			result.add(profile.getId());
		}
		
		return result;
	}

	private List<String> collectSectionIds(List<Section> sections) {
		List<String> result = new ArrayList<>();
		
		for(Section section : sections) {
			result.add(section.getId());
		}
		
		return result;
	}

	private void removeDevicesAndAggregates() {
		if(displayedDevices.size() > 0) {
			for(Device device : displayedDevices) {
				mapPresenter.removeDevice(device);
			}
			
			displayedDevices.clear();
		}
		
		if(displayedDeviceAggregations.size() > 0) {
			for(DeviceAggregate deviceAggregate : displayedDeviceAggregations) {
				mapPresenter.removeDeviceAggregate(deviceAggregate);
			}
			
			displayedDeviceAggregations.clear();
		}
	}
}