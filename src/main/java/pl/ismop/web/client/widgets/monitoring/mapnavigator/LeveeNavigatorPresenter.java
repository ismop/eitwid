package pl.ismop.web.client.widgets.monitoring.mapnavigator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.gwt.core.shared.GWT;
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

	@Inject
	public LeveeNavigatorPresenter(DapController dapController) {
		this.dapController = dapController;
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
		GWT.log("Profile clicked with id " + profile.getId());
	}
	
	public void onSectionClicked(final Section section) {
		mapPresenter.zoomOnSection(section);
		//TODO(DH): add spinner somewhere
		dapController.getDeviceAggregationForType("fiber", new DeviceAggregationsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processDeviceAggregations(List<DeviceAggregation> deviceAggreagations) {
				List<String> deviceAggregationIds = new ArrayList<>();
				
				for(DeviceAggregation deviceAggregation : deviceAggreagations) {
					deviceAggregationIds.add(deviceAggregation.getId());
				}
				
				dapController.getDevicesRecursivelyForAggregates(deviceAggregationIds, new DevicesCallback() {
					@Override
					public void onError(ErrorDetails errorDetails) {
						eventBus.showError(errorDetails);
					}
					
					@Override
					public void processDevices(final List<Device> fibreDevices) {
						dapController.getDevicesForSection(section.getId(), new DevicesCallback() {
							@Override
							public void onError(ErrorDetails errorDetails) {
								eventBus.showError(errorDetails);
							}
							
							@Override
							public void processDevices(List<Device> devices) {
								List<Device> filteredDevices = getRepeatingDevices(devices, fibreDevices);
								
								for(Device device : filteredDevices) {
									mapPresenter.addDevice(device);
								}
							}
						});
						
					}
				});
			}
		});
	}
	
	private List<Device> getRepeatingDevices(List<Device> devices, List<Device> fibreDevices) {
		List<Device> result = new ArrayList<>();
		
		for(Device device : devices) {
			for(Device fibreDevice : fibreDevices) {
				if(device.getId().equals(fibreDevice.getId())) {
					result.add(device);
					
					break;
				}
			}
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
}