package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSlicePresenter;
import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.IHorizontalSliceWizardView.IHorizontalSliceWizardPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;

@Presenter(view = HorizontalSliceWizardView.class)
public class HorizontalSliceWizardPresenter extends BasePresenter<IHorizontalSliceWizardView, MainEventBus> implements IHorizontalSliceWizardPresenter {
	private static final double HEIGHT_THRESHOLD = 0.2;
	
	private MapPresenter mapPresenter;
	
	private DapController dapController;
	
	private boolean configMode;

	private HorizontalCrosssectionConfiguration configuration;

	@Inject
	public HorizontalSliceWizardPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowHorizontalCrosssectionWizard() {
		view.clearProfiles();
		view.clearParameters();
		configMode = false;
		view.showButtonConfigLabel(false);
		
		configuration = new HorizontalCrosssectionConfiguration();
		view.showModal(true);
	}
	
	public void onShowHorizontalCrosssectionWizardWithConfig(HorizontalCrosssectionConfiguration configuration) {
		onShowHorizontalCrosssectionWizard();
		this.configuration = configuration;
		initializeConfigMode();
	}
	
	public void onProfileClicked(final Profile profile) {
		if(!configuration.getPickedProfiles().containsKey(profile.getId())) {
			view.addProfile(profile.getId());
			configuration.getPickedProfiles().put(profile.getId(), profile);
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
						
						if(configuration.getProfileDevicesMap().get(profile) == null) {
							configuration.getProfileDevicesMap().put(profile, new ArrayList<Device>());
						}
						
						configuration.getProfileDevicesMap().get(profile).add(device);
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
							
							for(Parameter parameter : parameters) {
								configuration.getParameterMap().put(parameter.getId(), parameter);
							}
							
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
					configuration.getSections().put(section.getId(), section);
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
		if(configuration.getPickedProfiles().containsKey(profileId)) {
			Profile removedProfile = configuration.getPickedProfiles().remove(profileId);
			String pickedHeight = configuration.getPickedHeights().remove(removedProfile);
			configuration.getHeightDevicesmap().remove(pickedHeight);
			view.removeProfile(profileId);
			
			if(configuration.getPickedProfiles().size() == 0) {
				view.showNoProfileLabel();
			}
			
			//updating parameter list
			List<Parameter> parameters = new ArrayList<>();
			
			for(Profile profile : configuration.getPickedProfiles().values()) {
				for(Device device : configuration.getProfileDevicesMap().get(profile)) {
					for(String parameterId : device.getParameterIds()) {
						parameters.add(configuration.getParameterMap().get(parameterId));
					}
				}
			}
			
			updateParameters(parameters);
		}
	}

	@Override
	public void onAcceptConfig() {
		if(configuration.getPickedProfiles().size() == 0 || configuration.getPickedParameterName() == null) {
			view.showNoProfilePickedError();
		} else {
			if(configMode) {
				eventBus.updateHorizontalSliceConfiguration(configuration);
			} else {
				HorizontalSlicePresenter horizontalSlicePresenter = eventBus.addHandler(HorizontalSlicePresenter.class);
				horizontalSlicePresenter.setConfiguration(configuration);
				eventBus.addPanel(view.getFullPanelTitle(), horizontalSlicePresenter);
			}
			
			view.showModal(false);
		}
	}

	@Override
	public void onChangePickedHeight(String profileId, String height) {
		configuration.getPickedHeights().put(configuration.getPickedProfiles().get(profileId), height);
	}

	@Override
	public void onParameterChanged(String parameterName) {
		configuration.setPickedParameterName(parameterName);
	}

	private void initializeConfigMode() {
		configMode = true;
		view.showButtonConfigLabel(true);
		
		for(String parameterName : configuration.getParameterNames()) {
			if(parameterName.equals(configuration.getPickedParameterName())) {
				view.addParameter(parameterName, true);
			} else {
				view.addParameter(parameterName, false);
			}
		}
		
		for(String profileId : configuration.getPickedProfiles().keySet()) {
			view.addProfile(profileId);
			
			Profile profile = configuration.getPickedProfiles().get(profileId);
			
			for(String height : configuration.getProfileHeights().get(profile)) {
				view.addProfileHeight(Double.parseDouble(height), profileId, configuration.getPickedHeights().get(profile).equals(height));
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
			Profile profile = configuration.getPickedProfiles().get(profileId);
			configuration.getProfileHeights().put(profile, new ArrayList<String>());
			
			for(Double height : result.keySet()) {
				configuration.getProfileHeights().get(profile).add(String.valueOf(height));
				view.addProfileHeight(height, profileId, first);
				
				if(first) {
					String heightValue = String.valueOf(height);
					configuration.getPickedHeights().put(profile, heightValue);
					configuration.getHeightDevicesmap().put(heightValue, result.get(height));
				}
				
				first = false;
			}
		}
	}

	private void updateParameters(List<Parameter> parameters) {
		Set<String> result = new HashSet<>();
		
		for(Parameter parameter : parameters) {
			result.add(parameter.getMeasurementTypeName());
		}
		
		for(Iterator<String> i = configuration.getParameterNames().iterator(); i.hasNext();) {
			String parameterName = i.next();
			
			if(!result.contains(parameterName)) {
				i.remove();
				configuration.getParameterNames().remove(parameterName);
				view.removeParameter(parameterName);
				
				if(configuration.getPickedParameterName().equals(parameterName)) {
					configuration.setPickedParameterName(null);
				}
			}
		}
		
		for(String parameterName : result) {
			if(!configuration.getParameterNames().contains(parameterName)) {
				if(configuration.getPickedParameterName() == null) {
					view.addParameter(parameterName, true);
					configuration.setPickedParameterName(parameterName);
				} else {
					view.addParameter(parameterName, false);
				}
				
				configuration.getParameterNames().add(parameterName);
			}
		}
		
		
		if(configuration.getParameterNames().size() == 0) {
			view.showNoParamtersLabel(true);
		}
	}
}