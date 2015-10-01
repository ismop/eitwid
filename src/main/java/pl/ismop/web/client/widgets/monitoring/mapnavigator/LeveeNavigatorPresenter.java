package pl.ismop.web.client.widgets.monitoring.mapnavigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
	
	private Map<String, Device> selectedDevices, profileDevices;
	
	private Section selectedSection;
	
	private Map<String, DeviceAggregate> selectedDeviceAggregates;
	
	private SideProfilePresenter profilePresenter;
	
	private Map<String, Profile> profiles;

	@Inject
	public LeveeNavigatorPresenter(DapController dapController) {
		this.dapController = dapController;
		displayedDevices = new ArrayList<>();
		displayedDeviceAggregations = new ArrayList<>();
		selectedDevices = new HashMap<>();
		selectedDeviceAggregates = new HashMap<>();
		profileDevices = new HashMap<>();
		profiles = new HashMap<>();
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
			profiles.clear();
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
								LeveeNavigatorPresenter.this.profiles.put(profile.getId(), profile);
							}
						}
					});
				}
			});
		}
	}
	
	public void onProfileClicked(final Profile profile) {
		if(profilePresenter == null) {
			profilePresenter = eventBus.addHandler(SideProfilePresenter.class);
			profilePresenter.setWidthAndHeight(view.getProfileContainerWidth(), view.getProfileContainerHeight());
			view.setProfile(profilePresenter.getView());
		}
		
		view.showMap(false);
		view.showProfile(true);
		profilePresenter.clear();
		profileDevices.clear();
		dapController.getDevicesRecursively(profile.getId(), new DevicesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processDevices(List<Device> devices) {
				profilePresenter.setProfileAndDevices(profile, devices);
				
				for(Device device : devices) {
					profileDevices.put(device.getId(), device);
					
					if(selectedDevices.keySet().contains(device.getId())) {
						profilePresenter.markDevice(device.getId(), true);
					}
				}
			}
		});
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
			
			if(selectedSection == null || !device.getSectionId().equals(selectedSection.getId())) {
				selectedDevices.remove(device.getId());
				mapPresenter.removeDevice(device);
			}
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
						
						if(selectedDevices.containsKey(device.getId())) {
							selectedDevices.remove(device.getId());
						}
					}
					
					
					if(selectedSection == null || !profiles.get(deviceAggregate.getProfileId()).getSectionId().equals(selectedSection.getId())) {
						selectedDeviceAggregates.remove(deviceAggregate.getId());
						mapPresenter.removeDeviceAggregate(deviceAggregate);
					}
				} else {
					mapPresenter.selectDeviceAggregate(deviceAggregate, true);
					selectedDeviceAggregates.put(deviceAggregate.getId(), deviceAggregate);
					
					for(Device device : devices) {
						eventBus.deviceSelected(device, true);
						
						if(!selectedDevices.containsKey(device.getId())) {
							selectedDevices.put(device.getId(), device);
						}
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
	
	public void onBackFromSideProfile() {
		view.showMap(true);
		view.showProfile(false);
	}
	
	public void onDevicesHovered(List<String> deviceIds, boolean hovered) {
		if(profileDevices.containsKey(deviceIds.get(0))) {
			eventBus.showDeviceMetadata(profileDevices.get(deviceIds.get(0)), hovered);
		}
	}
	
	public void onDevicesClicked(List<String> deviceIds) {
		for(String deviceId : deviceIds) {
			if(selectedDevices.containsKey(deviceId)) {
				selectedDevices.remove(deviceId);
				eventBus.deviceSelected(profileDevices.get(deviceId), false);
				profilePresenter.markDevice(deviceId, false);
				
				for(DeviceAggregate deviceAggregate : displayedDeviceAggregations) {
					if(deviceAggregate.getDeviceIds().containsAll(deviceIds) && selectedDeviceAggregates.containsKey(deviceAggregate.getId())) {
						mapPresenter.selectDeviceAggregate(deviceAggregate, false);
						selectedDeviceAggregates.remove(deviceAggregate.getId());
						
						break;
					}
				}
			} else {
				selectedDevices.put(deviceId, profileDevices.get(deviceId));
				eventBus.deviceSelected(profileDevices.get(deviceId), true);
				profilePresenter.markDevice(deviceId, true);
				
				boolean found = false;
				
				for(DeviceAggregate deviceAggregate : displayedDeviceAggregations) {
					if(deviceAggregate.getDeviceIds().containsAll(deviceIds)) {
						found = true;
						
						if(!selectedDeviceAggregates.containsKey(deviceAggregate.getId())) {
							mapPresenter.addDeviceAggregate(deviceAggregate);
							mapPresenter.selectDeviceAggregate(deviceAggregate, true);
							selectedDeviceAggregates.put(deviceAggregate.getId(), deviceAggregate);
							found = true;
							
							break;
						}
					}
				}
				
				if(!found) {
					dapController.getDeviceAggregation(profileDevices.get(deviceId).getDeviceAggregationId(), new DeviceAggregatesCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processDeviceAggregations(List<DeviceAggregate> deviceAggreagations) {
							if(deviceAggreagations.size() > 0) {
								mapPresenter.addDeviceAggregate(deviceAggreagations.get(0));
								mapPresenter.selectDeviceAggregate(deviceAggreagations.get(0), true);
								selectedDeviceAggregates.put(deviceAggreagations.get(0).getId(), deviceAggreagations.get(0));
							}
						}
					});
				}
			}
		}
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
			for(Iterator<Device> i = displayedDevices.iterator(); i.hasNext();) {
				Device device = i.next();
				
				if(!selectedDevices.containsKey(device.getId())) {
					i.remove();
					mapPresenter.removeDevice(device);
				}
			}
		}
		
		if(displayedDeviceAggregations.size() > 0) {
			for(Iterator<DeviceAggregate> i = displayedDeviceAggregations.iterator(); i.hasNext();) {
				DeviceAggregate deviceAggregate = i.next();
				
				if(!selectedDeviceAggregates.containsKey(deviceAggregate.getId())) {
					i.remove();
					mapPresenter.removeDeviceAggregate(deviceAggregate);
				}
			}
		}
	}
}