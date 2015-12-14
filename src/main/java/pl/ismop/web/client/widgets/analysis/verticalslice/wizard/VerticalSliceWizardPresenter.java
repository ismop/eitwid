package pl.ismop.web.client.widgets.analysis.verticalslice.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import pl.ismop.web.client.dap.DapController.ScenariosCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.scenario.Scenario;
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

	private Experiment experiment;

	@Inject
	public VerticalSliceWizardPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onShowVerticalCrosssectionWizard(Experiment experiment) {
		this.experiment = experiment;
		view.clearProfiles();
		view.clearParameters();
		configMode = false;
		view.showButtonConfigLabel(false);
		
		configuration = new VerticalCrosssectionConfiguration();
		configuration.setExperiment(experiment);
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
						public void processParameters(final List<Parameter> parameters) {
							for(Parameter parameter : parameters) {
								configuration.getParameterMap().put(parameter.getId(), parameter);
							}
							
							dapController.getExperimentScenarios(experiment.getId(), new ScenariosCallback() {
								@Override
								public void onError(ErrorDetails errorDetails) {
									view.showLoadingState(false);
									eventBus.showError(errorDetails);
								}
								
								@Override
								public void processScenarios(List<Scenario> scenarios) {
									view.showLoadingState(false);
									
									for(Scenario scenario : scenarios) {
										configuration.getScenarioMap().put(scenario.getId(), scenario);
									}
									
									updateParametersAndScenarios(parameters, scenarios);
								}
							});
						}
					});
				}
			});
		}
	}
	
	public void onShowVerticalCrosssectionWizardWithConfig(VerticalCrosssectionConfiguration configuration) {
		onShowVerticalCrosssectionWizard(experiment);
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

	@Override
	public void onDataSelectorChanged(String dataSelector) {
		configuration.setDataSelector(dataSelector);
	}

	private void initializeConfigMode() {
		configMode = true;
		view.showButtonConfigLabel(true);
		
		Map<String, Integer> counter = countParameters(configuration.getParameterMap().values());
		
		for(String parameterName : configuration.getParameterNames()) {
			if(parameterName.equals(configuration.getPickedParameterName())) {
				view.addParameter(parameterName, true, counter.get(parameterName) > 1);
			} else {
				view.addParameter(parameterName, false, counter.get(parameterName) > 1);
			}
		}
		
		view.setProfile(configuration.getPickedProfile().getId());
		processScenarios(new ArrayList<>(configuration.getScenarioMap().values()));
		view.selectScenario(configuration.getDataSelector());
	}

	private void updateParametersAndScenarios(List<Parameter> parameters, List<Scenario> scenarios) {
		configuration.setDataSelector("0");
		
		Set<String> result = new HashSet<>();
		Map<String, Integer> counter = countParameters(parameters);
		
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
					view.addParameter(parameterName, true, counter.get(parameterName) > 1);
					configuration.setPickedParameterName(parameterName);
				} else {
					view.addParameter(parameterName, false, counter.get(parameterName) > 1);
				}
				
				configuration.getParameterNames().add(parameterName);
			}
		}
		
		
		if(configuration.getParameterNames().size() == 0) {
			view.showNoParamtersLabel(true);
		} else {
			processScenarios(scenarios);
		}
	}

	private void processScenarios(List<Scenario> scenarios) {
		Map<String, String> scenariosMap = new LinkedHashMap<>();
		scenariosMap.put("0", view.getRealDataLabel());
		
		for(Scenario scenario : scenarios) {
			scenariosMap.put(scenario.getId(), view.getScenarioNamePrefix() + " " + scenario.getName());
		}
		
		view.addScenarios(scenariosMap);
	}

	private Map<String, Integer> countParameters(Collection<Parameter> parameters) {
		Map<String, Integer> result = new HashMap<>();
		
		for(Parameter parameter : parameters) {
			result.put(parameter.getMeasurementTypeName(), result.get(
					parameter.getMeasurementTypeName()) == null ? 1 : result.get(parameter.getMeasurementTypeName()) + 1);
		}
		
		return result;
	}
}