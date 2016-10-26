package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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

import javaslang.Tuple2;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.FunctionalDapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorUtil;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalCrosssectionConfiguration;
import pl.ismop.web.client.widgets.analysis.horizontalslice.HorizontalSlicePresenter;
import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.IHorizontalSliceWizardView.IHorizontalSliceWizardPresenter;
import pl.ismop.web.client.widgets.common.map.MapPresenter;

@Presenter(view = HorizontalSliceWizardView.class)
public class HorizontalSliceWizardPresenter extends BasePresenter<IHorizontalSliceWizardView,
		MainEventBus> implements IHorizontalSliceWizardPresenter {

	private static final double HEIGHT_THRESHOLD = 0.2;

	private MapPresenter mapPresenter;

	private FunctionalDapController dapController;

	private boolean configMode;

	private HorizontalCrosssectionConfiguration configuration;

	private Experiment experiment;

	private ErrorUtil errorUtil;

	@Inject
	public HorizontalSliceWizardPresenter(FunctionalDapController dapController,
			ErrorUtil errorUtil) {
		this.dapController = dapController;
		this.errorUtil = errorUtil;
	}

	public void onShowHorizontalCrosssectionWizard(Experiment experiment) {
		this.experiment = experiment;
		view.clearProfiles();
		view.clearParameters();
		configMode = false;
		view.showButtonConfigLabel(false);

		configuration = new HorizontalCrosssectionConfiguration();
		configuration.setExperiment(experiment);
		view.showModal(true);
	}

	public void onShowHorizontalCrosssectionWizardWithConfig(
			HorizontalCrosssectionConfiguration configuration) {
		onShowHorizontalCrosssectionWizard(experiment);
		this.configuration = configuration;
		initializeConfigMode();
	}

	public void onProfileClicked(final Profile profile) {
		for(Profile pickedProfile : configuration.getPickedProfiles().values()) {
			if(pickedProfile.getSectionId().equals(profile.getSectionId())) {
				eventBus.showSimpleError(view.singleProfilePerSection());

				return;
			}
		}

		if(!configuration.getPickedProfiles().containsKey(profile.getId())) {
			view.addProfile(profile.getId());
			configuration.getPickedProfiles().put(profile.getId(), profile);
			view.showLoadingState(true, profile.getId());
			dapController.getAllDevicesForProfile(profile.getId())
			.onSuccess(devices -> {
				if(configuration.getProfileDevicesMap().get(profile) == null) {
					configuration.getProfileDevicesMap().put(profile,
							new ArrayList<Device>());
				}

				configuration.getProfileDevicesMap().get(profile).addAll(devices.toJavaList());
				dapController.getParameters(devices.map(Device::getId))
				.onSuccess(parameters -> {
					dapController.getExperimentScenarios(experiment.getId())
					.onSuccess(scenarios -> {
						view.showLoadingState(false, profile.getId());

						for(Parameter parameter : parameters) {
							configuration.getParameterMap().put(parameter.getId(), parameter);
						}

						for(Scenario scenario : scenarios) {
							configuration.getScenarioMap().put(scenario.getId(), scenario);
						}

						computeHeights(devices.toJavaList(), profile.getId());
						updateParametersAndScenarios(
								parameters.toJavaList(), scenarios.toJavaList());
					})
					.onFailure(e -> handleCommunicationErrors(e, profile));
				})
				.onFailure(e -> handleCommunicationErrors(e, profile));
			})
			.onFailure(e -> handleCommunicationErrors(e, profile));
		}
	}

	@Override
	public void onModalShown() {
		if(mapPresenter == null) {
			mapPresenter = eventBus.addHandler(MapPresenter.class);
			mapPresenter.addClickListeners();
			mapPresenter.setMoveable(true);
			view.setMap(mapPresenter.getView());
		}

		mapPresenter.reset(false);
		mapPresenter.setLoadingState(true);
		//TODO: fetch only sections for a levee which should be passed
		//by the event bus in the future
		dapController.getSections()
		.onSuccess(sections -> {
			configuration.getSections().putAll(sections.toJavaMap(
					section -> new Tuple2<>(section.getId(), section)));
			sections.forEach(mapPresenter::add);
			dapController.getProfiles(sections.map(Section::getId))
			.onSuccess(profiles -> {
				mapPresenter.setLoadingState(false);
				profiles.forEach(mapPresenter::add);
			})
			.onFailure(this::handleCommunicationErrors);
		})
		.onFailure(this::handleCommunicationErrors);
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

			//updating parameter list
			List<Parameter> parameters = new ArrayList<>();

			for(Profile profile : configuration.getPickedProfiles().values()) {
				for(Device device : configuration.getProfileDevicesMap().get(profile)) {
					for(String parameterId : device.getParameterIds()) {
						parameters.add(configuration.getParameterMap().get(parameterId));
					}
				}
			}

			updateParametersAndScenarios(parameters, new ArrayList<>(
					configuration.getScenarioMap().values()));
		}
	}

	@Override
	public void onAcceptConfig() {
		if(configuration.getPickedProfiles().size() == 0
				|| configuration.getPickedParameterMeasurementName() == null) {
			eventBus.showSimpleError(view.noProfilePickedError());
		} else {
			if(configMode) {
				eventBus.updateHorizontalSliceConfiguration(configuration);
			} else {
				HorizontalSlicePresenter horizontalSlicePresenter = eventBus.addHandler(
						HorizontalSlicePresenter.class);
				horizontalSlicePresenter.setConfiguration(configuration);
				eventBus.addPanel(view.getFullPanelTitle(), horizontalSlicePresenter);
			}

			view.showModal(false);
		}
	}

	@Override
	public void onChangePickedHeight(String profileId, String height) {
		configuration.getPickedHeights().put(configuration.getPickedProfiles().get(profileId),
				height);
	}

	@Override
	public void onParameterChanged(String parameterName) {
		configuration.setPickedParameterMeasurementName(parameterName);
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
			if(parameterName.equals(configuration.getPickedParameterMeasurementName())) {
				view.addParameter(parameterName, true, counter.get(parameterName) > 1);
			} else {
				view.addParameter(parameterName, false, counter.get(parameterName) > 1);
			}
		}

		for(String profileId : configuration.getPickedProfiles().keySet()) {
			view.addProfile(profileId);

			Profile profile = configuration.getPickedProfiles().get(profileId);

			for(String height : configuration.getProfileHeights().get(profile)) {
				view.addProfileHeight(Double.parseDouble(height), profileId,
						configuration.getPickedHeights().get(profile).equals(height));
			}
		}

		processScenarios(new ArrayList<>(configuration.getScenarioMap().values()));
		view.selectScenario(configuration.getDataSelector());
	}

	private void computeHeights(List<Device> devices, String profileId) {
		Map<Double, List<Device>> result = new HashMap<>();

		if(devices.size() > 0) {
			for(Iterator<Device> i = devices.iterator(); i.hasNext();) {
				Device device = i.next();

				if(device.getPlacement() == null
						|| device.getPlacement().getCoordinates().size() < 2) {
					i.remove();
				}
			}
			sort(devices, new Comparator<Device>() {
				@Override
				public int compare(Device o1, Device o2) {
					return o1.getPlacement().getCoordinates().get(2).compareTo(
							o2.getPlacement().getCoordinates().get(2));
				}
			});

			double threshold = HEIGHT_THRESHOLD + devices.get(0).getPlacement().
					getCoordinates().get(2);

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

	private void updateParametersAndScenarios(List<Parameter> parameters,
			List<Scenario> scenarios) {
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

				if(configuration.getPickedParameterMeasurementName().equals(parameterName)) {
					configuration.setPickedParameterMeasurementName(null);
				}
			}
		}

		for(String parameterName : result) {
			if(!configuration.getParameterNames().contains(parameterName)) {
				if(configuration.getPickedParameterMeasurementName() == null) {
					view.addParameter(parameterName, true, counter.get(parameterName) > 1);
					configuration.setPickedParameterMeasurementName(parameterName);
				} else {
					view.addParameter(parameterName, false, counter.get(parameterName) > 1);
				}

				configuration.getParameterNames().add(parameterName);
			}
		}

		if (configuration.getParameterNames().size() == 0) {
			view.showNoParamtersLabel(true);
		} else {
			processScenarios(scenarios);
		}

		if (configuration.getParameterNames().size() == 0) {
			view.showNoParamtersLabel(true);
		}
	}

	private void processScenarios(List<Scenario> scenarios) {
		Map<String, String> scenariosMap = new LinkedHashMap<>();
		scenariosMap.put("0", view.getRealDataLabel());

		for(Scenario scenario : scenarios) {
			scenariosMap.put(scenario.getId(), view.getScenarioNamePrefix() + " "
					+ scenario.getName());
		}

		view.addScenarios(scenariosMap);
	}

	private Map<String, Integer> countParameters(Collection<Parameter> parameters) {
		Map<String, Integer> result = new HashMap<>();

		for(Parameter parameter : parameters) {
			result.put(parameter.getMeasurementTypeName(), result.get(
					parameter.getMeasurementTypeName()) == null ? 1
							: result.get(parameter.getMeasurementTypeName()) + 1);
		}

		return result;
	}

	private void handleCommunicationErrors(Throwable e, Profile profile) {
		view.showLoadingState(false, profile.getId());
		eventBus.showError(errorUtil.processErrors(null, e));
	}

	private void handleCommunicationErrors(Throwable e) {
		mapPresenter.setLoadingState(false);
		eventBus.showError(errorUtil.processErrors(null, e));
	}
}
