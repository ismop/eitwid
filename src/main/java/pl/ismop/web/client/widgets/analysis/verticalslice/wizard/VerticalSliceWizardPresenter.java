package pl.ismop.web.client.widgets.analysis.verticalslice.wizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.verticalslice.VerticalSlicePresenter;
import pl.ismop.web.client.widgets.analysis.verticalslice.wizard.IVerticalSliceWizardView.IVerticalSliceWizardPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;

@Presenter(view = VerticalSliceWizardView.class)
public class VerticalSliceWizardPresenter extends BasePresenter<IVerticalSliceWizardView, MainEventBus> implements IVerticalSliceWizardPresenter {
	private DapController dapController;
	
	private MapPresenter mapPresenter;
	
	private VerticalCrosssectionConfiguration configuration;
	
	private boolean configMode;

	@Inject
	public VerticalSliceWizardPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowVerticalCrosssectionWizard() {
		view.clearProfiles();
		view.clearParameters();
		configMode = false;
		view.showButtonConfigLabel(false);
		
		configuration = new VerticalCrosssectionConfiguration();
		view.showModal(true);
	}
	
	public void onProfileClicked(final Profile profile) {
		if(configuration.getPickedProfile() == null || !configuration.getPickedProfile().getId().equals(profile.getId())) {
			view.setProfile(profile.getId());
			configuration.setPickedProfile(profile);
			view.showLoadingState(true);
			dapController.getDevicesRecursively(profile.getId(), new DevicesCallback() {
				@Override
				public void onError(ErrorDetails errorDetails) {
					view.showLoadingState(false);
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
							view.showLoadingState(false);
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processParameters(List<Parameter> parameters) {
							view.showLoadingState(false);
							
							for(Parameter parameter : parameters) {
								configuration.getParameterMap().put(parameter.getId(), parameter);
							}
							
							updateParameters(parameters);
						}
					});
				}
			});
		}
	}
	
	public void onShowVerticalCrosssectionWizardWithConfig(VerticalCrosssectionConfiguration configuration) {
		onShowVerticalCrosssectionWizard();
		this.configuration = configuration;
		initializeConfigMode();
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
//					configuration.getSections().put(section.getId(), section);
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
		eventBus.verticalCrosssectionWizardHidden();
	}

	@Override
	public void onParameterChanged(String parameterName) {
		configuration.setPickedParameterName(parameterName);
	}

	@Override
	public void onAcceptConfig() {
		if(configuration.getPickedProfile() == null) {
			view.showNoProfilePickedError();
		} else {
			if(configMode) {
				eventBus.updateVerticalSliceConfiguration(configuration);
			} else {
				VerticalSlicePresenter presenter = eventBus.addHandler(VerticalSlicePresenter.class);
				presenter.setConfiguration(configuration);
				eventBus.addPanel(view.getFullPanelTitle(), presenter);
			}
			
			view.showModal(false);
		}
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
		
		view.setProfile(configuration.getPickedProfile().getId());
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