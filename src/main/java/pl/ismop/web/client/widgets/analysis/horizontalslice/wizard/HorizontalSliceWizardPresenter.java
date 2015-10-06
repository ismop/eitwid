package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.DevicesCallback;
import pl.ismop.web.client.dap.DapController.ParametersCallback;
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.IHorizontalSliceWizardView.IHorizontalSliceWizardPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;

@Presenter(view = HorizontalSliceWizardView.class)
public class HorizontalSliceWizardPresenter extends BasePresenter<IHorizontalSliceWizardView, MainEventBus> implements IHorizontalSliceWizardPresenter {
	private static final double HEIGHT_THRESHOLD = 0.2;
	
	private MapPresenter mapPresenter;
	
	private DapController dapController;
	
	private Map<String, Profile> pickedProfiles;

	@Inject
	public HorizontalSliceWizardPresenter(DapController dapController) {
		this.dapController = dapController;
		pickedProfiles = new HashMap<>();
	}
	
	public void onShowHorizontalCrosssectionWizard() {
		pickedProfiles.clear();
		view.clearProfiles();
		view.showModal(true);
	}
	
	public void onProfileClicked(final Profile profile) {
		if(!pickedProfiles.containsKey(profile.getId())) {
			view.addProfile(profile.getId());
			pickedProfiles.put(profile.getId(), profile);
			view.showLoadingState(true, profile.getId());
			dapController.getDevicesRecursively(profile.getId(), new DevicesCallback() {
				@Override
				public void onError(ErrorDetails errorDetails) {
					view.showLoadingState(false, profile.getId());
					eventBus.showError(errorDetails);
				}
				
				@Override
				public void processDevices(final List<Device> devices) {
					List<String> deviceIds = new ArrayList<>();
					
					for(Device device : devices) {
						deviceIds.add(device.getId());
					}
					
					dapController.getParameters(deviceIds, new ParametersCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showLoadingState(false, profile.getId());
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processParameters(List<Parameter> parameters) {
							view.showLoadingState(false, profile.getId());
							computeHeights(devices, profile.getId());
							updateParameters(parameters);
						}
					});
				}
			});
		}
	}

	@Override
	public void onModalShown() {
		if(mapPresenter == null) {
			mapPresenter = eventBus.addHandler(MapPresenter.class);
			mapPresenter.addClickListeners();
			view.setMap(mapPresenter.getView());
		}
		
		mapPresenter.reset(false);
		mapPresenter.setLoadingState(true);
		//TODO: fetch only sections for a levee which should be passed by the event bus in the future
		dapController.getSections(new SectionsCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				mapPresenter.setLoadingState(false);
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processSections(List<Section> sections) {
				List<String> sectionIds = new ArrayList<>();
				
				for(Section section : sections) {
					mapPresenter.addSection(section);
					sectionIds.add(section.getId());
				}
				
				dapController.getProfiles(sectionIds, new ProfilesCallback() {
					@Override
					public void onError(ErrorDetails errorDetails) {
						mapPresenter.setLoadingState(false);
						eventBus.showError(errorDetails);
					}
					
					@Override
					public void processProfiles(List<Profile> profiles) {
						mapPresenter.setLoadingState(false);
						
						for(Profile profile : profiles) {
							mapPresenter.addProfile(profile);
						}
					}
				});
			}
		});
	}

	@Override
	public void onModalHide() {
		eventBus.horizontalCrosssectionWizardHidden();
	}

	@Override
	public void onRemoveProfile(String profileId) {
		if(pickedProfiles.containsKey(profileId)) {
			pickedProfiles.remove(profileId);
			view.removeProfile(profileId);
			
			if(pickedProfiles.size() == 0) {
				view.showNoProfileLabel();
			}
		}
	}

	private void computeHeights(List<Device> devices, String profileId) {
		Map<Double, List<Device>> result = new HashMap<>();
		
		if(devices.size() > 0) {
			for(Iterator<Device> i = devices.iterator(); i.hasNext();) {
				Device device = i.next();
				
				if(device.getPlacement() == null || device.getPlacement().getCoordinates().size() < 2) {
					i.remove();
				}
			}
			sort(devices, new Comparator<Device>() {
				@Override
				public int compare(Device o1, Device o2) {
					return o1.getPlacement().getCoordinates().get(2).compareTo(o2.getPlacement().getCoordinates().get(2));
				}
			});
			
			double threshold = HEIGHT_THRESHOLD + devices.get(0).getPlacement().getCoordinates().get(2);
			
			for(Device device : devices) {
				while(device.getPlacement().getCoordinates().get(2) > threshold) {
					threshold += HEIGHT_THRESHOLD;
				}
				
				if(result.get(threshold) == null) {
					result.put(threshold, new ArrayList<Device>());
				}
				
				result.get(threshold).add(device);
			}
			
			boolean first = true;
			
			for(Double height : result.keySet()) {
				view.addProfileHeight(height, profileId, first);
				first = false;
			}
		}
	}

	private void updateParameters(List<Parameter> parameters) {
		
	}
}