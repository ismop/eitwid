package pl.ismop.web.client.widgets.monitoring.mapnavigator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.DeviceAggregationsCallback;
import pl.ismop.web.client.dap.DapController.DevicesCallback;
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.monitoring.mapnavigator.ILeveeNavigatorView.ILeveeNavigatorPresenter;

@Presenter(view = LeveeNavigatorView.class, multiple = true)
public class LeveeNavigatorPresenter extends BasePresenter<ILeveeNavigatorView, MainEventBus> implements ILeveeNavigatorPresenter {
	private MapPresenter mapPresenter;
	private Levee displayedLevee;
	private DapController dapController;
	private List<Device> displayedDevices;
	private List<DeviceAggregation> displayedDeviceAggregations;

	@Inject
	public LeveeNavigatorPresenter(DapController dapController) {
		this.dapController = dapController;
		displayedDevices = new ArrayList<>();
		displayedDeviceAggregations = new ArrayList<>();
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
			
			mapPresenter.reset();
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
		Window.alert("To be implemented. Profile view will be shown in the main panel.");
	}
	
	public void onSectionClicked(final Section section) {
		removeDevicesAndAggregates();
		
		mapPresenter.zoomOnSection(section);
		mapPresenter.addAction(section.getId(), view.getZoomOutLabel());
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
				dapController.getDeviceAggregations(collectProfileIds(profiles), new DeviceAggregationsCallback() {
					@Override
					public void onError(ErrorDetails errorDetails) {
						eventBus.showError(errorDetails);
					}
					
					@Override
					public void processDeviceAggregations(List<DeviceAggregation> deviceAggreagations) {
						displayedDeviceAggregations.addAll(deviceAggreagations);
						
						for(DeviceAggregation deviceAggregation : deviceAggreagations) {
							mapPresenter.addDeviceAggregation(deviceAggregation);
						}
					}
				});
			}
		});
	}

	public void onDeviceClicked(Device device) {
		Window.alert("TODO");
	}
	
	public void onDeviceAggregateClicked(DeviceAggregation deviceAggregation) {
		Window.alert("TODO");
	}
	
	public void onZoomOut(String sectionId) {
		removeDevicesAndAggregates();
		mapPresenter.zoomToAllSections();
		mapPresenter.removeAction(sectionId);
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
			for(DeviceAggregation deviceAggregation : displayedDeviceAggregations) {
				mapPresenter.removeDeviceAggregation(deviceAggregation);
			}
			
			displayedDeviceAggregations.clear();
		}
	}
}