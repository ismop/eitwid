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

	private static final double HEIGHT_THRESHOLD = 1.0;

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
		view.clearSections();
		view.clearParameters();
		configMode = false;
		view.showButtonConfigLabel(false);

		configuration = new HorizontalCrosssectionConfiguration();
		view.setBudokopProfilesToggle(configuration.isBudokopProfiles());
		configuration.setExperiment(experiment);
		view.showModal(true);
	}

	public void onShowHorizontalCrosssectionWizardWithConfig(
			HorizontalCrosssectionConfiguration configuration) {
		onShowHorizontalCrosssectionWizard(experiment);
		this.configuration = configuration;
		initializeConfigMode();
	}

	public void onSectionClicked(Section section) {
		if (!configuration.getPickedSections().containsKey(section.getId())) {
			view.addSection(section.getId());
			configuration.getPickedSections().put(section.getId(), section);
			view.showLoadingState(true, section.getId());
			dapController.getProfiles(javaslang.collection.List.of(section.getId()))
				.flatMap(profiles -> dapController.getAllDevicesForProfiles(
						profiles
							.filter(profile -> profile.getVendors()
									.contains(configuration.isBudokopProfiles()
											? "budokop" : "neosentio"))
							.map(Profile::getId)))
				.onSuccess(devices -> {
					if(configuration.getSectionDevicesMap().get(section) == null) {
						configuration.getSectionDevicesMap().put(section, new ArrayList<Device>());
					}

					configuration.getSectionDevicesMap().get(section).addAll(devices.toJavaList());
					dapController.getParameters(devices.map(Device::getId))
					.onSuccess(parameters -> {
						dapController.getExperimentScenarios(experiment.getId())
						.onSuccess(scenarios -> {
							view.showLoadingState(false, section.getId());

							for(Parameter parameter : parameters) {
								configuration.getParameterMap().put(parameter.getId(), parameter);
							}

							for(Scenario scenario : scenarios) {
								configuration.getScenarioMap().put(scenario.getId(), scenario);
							}

							computeHeights(devices.toJavaList(), section.getId());
							updateParametersAndScenarios(
									parameters.toJavaList(), scenarios.toJavaList());
						})
						.onFailure(e -> handleCommunicationErrors(e, section));
					})
					.onFailure(e -> handleCommunicationErrors(e, section));
				})
				.onFailure(e -> handleCommunicationErrors(e, section));
		}
	}

	@Override
	public void onModalShown() {
		if (mapPresenter == null) {
			mapPresenter = eventBus.addHandler(MapPresenter.class);
			mapPresenter.addClickListeners();
			view.setMap(mapPresenter.getView());
		}

		if (!configMode) {
			refreshSections();
		}
	}

	@Override
	public void onModalHide() {
		eventBus.horizontalCrosssectionWizardHidden();
	}

	@Override
	public void onRemoveSection(String sectionId) {
		if(configuration.getPickedSections().containsKey(sectionId)) {
			Section removedSection = configuration.getPickedSections().remove(sectionId);
			String pickedHeight = configuration.getPickedHeights().remove(removedSection);
			configuration.getHeightDevicesmap().remove(pickedHeight);
			view.removeSection(sectionId);

			//updating parameter list
			List<Parameter> parameters = new ArrayList<>();

			for(Section profile : configuration.getPickedSections().values()) {
				for(Device device : configuration.getSectionDevicesMap().get(profile)) {
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
		if (configuration.getPickedSections().size() == 0
				|| configuration.getPickedParameterMeasurementName() == null) {
			eventBus.showSimpleError(view.noSectionPickedError());
		} else {
			if (configMode) {
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
	public void onChangePickedHeight(String sectionId, String height) {
		configuration.getPickedHeights().put(configuration.getPickedSections().get(sectionId),
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

	@Override
	public void onProfileTypeChange(boolean budokopType) {
		this.configuration.setBudokopProfiles(budokopType);
		refreshSections();
	}

	private void initializeConfigMode() {
		configMode = true;
		view.showButtonConfigLabel(true);
		view.setBudokopProfilesToggle(configuration.isBudokopProfiles());

		Map<String, Integer> counter = countParameters(configuration.getParameterMap().values());

		for (String parameterName : configuration.getParameterNames()) {
			if (parameterName.equals(configuration.getPickedParameterMeasurementName())) {
				view.addParameter(parameterName, true, counter.get(parameterName) > 1);
			} else {
				view.addParameter(parameterName, false, counter.get(parameterName) > 1);
			}
		}

		for (String sectionId : configuration.getPickedSections().keySet()) {
			view.addSection(sectionId);

			Section section = configuration.getPickedSections().get(sectionId);

			for (String height : configuration.getSectionHeights().get(section)) {
				view.addSectionHeight(Double.parseDouble(height), sectionId,
						configuration.getPickedHeights().get(section).equals(height));
			}
		}

		processScenarios(new ArrayList<>(configuration.getScenarioMap().values()));
		view.selectScenario(configuration.getDataSelector());
	}

	private void computeHeights(List<Device> devices, String sectionId) {
		Map<Double, List<Device>> result = new HashMap<>();

		if (devices.size() > 0) {
			for (Iterator<Device> i = devices.iterator(); i.hasNext();) {
				Device device = i.next();

				if (device.getPlacement() == null
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

			for (Device device : devices) {
				while (device.getPlacement().getCoordinates().get(2) > threshold) {
					threshold += HEIGHT_THRESHOLD;
				}

				if (result.get(threshold) == null) {
					result.put(threshold, new ArrayList<Device>());
				}

				result.get(threshold).add(device);
			}

			boolean first = true;
			Section section = configuration.getPickedSections().get(sectionId);
			configuration.getSectionHeights().put(section, new ArrayList<String>());

			for (Double height : result.keySet()) {
				configuration.getSectionHeights().get(section).add(String.valueOf(height));
				view.addSectionHeight(height, sectionId, first);

				if (first) {
					String heightValue = String.valueOf(height);
					configuration.getPickedHeights().put(section, heightValue);
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

		for (Parameter parameter : parameters) {
			result.add(parameter.getMeasurementTypeName());
		}

		for (Iterator<String> i = configuration.getParameterNames().iterator(); i.hasNext();) {
			String parameterName = i.next();

			if (!result.contains(parameterName)) {
				i.remove();
				configuration.getParameterNames().remove(parameterName);
				view.removeParameter(parameterName);

				if (parameterName.equals(configuration.getPickedParameterMeasurementName())) {
					configuration.setPickedParameterMeasurementName(null);
				}
			}
		}

		for (String parameterName : result) {
			if (!configuration.getParameterNames().contains(parameterName)) {
				if (configuration.getPickedParameterMeasurementName() == null) {
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

	private void handleCommunicationErrors(Throwable e, Section section) {
		view.showLoadingState(false, section.getId());
		eventBus.showError(errorUtil.processErrors(null, e));
	}

	private void handleCommunicationErrors(Throwable e) {
		mapPresenter.setLoadingState(false);
		eventBus.showError(errorUtil.processErrors(null, e));
	}

	private void refreshSections() {
		configuration.getPickedSections().keySet().forEach(view::removeSection);
		configuration.getPickedSections().clear();
		configuration.getPickedHeights().clear();
		configuration.getParameterNames().forEach(view::removeParameter);
		configuration.getParameterNames().clear();
		mapPresenter.reset(false);
		mapPresenter.setLoadingState(true);
		dapController.getSections()
		.onSuccess(sections -> {
			configuration.getSections().putAll(sections.toJavaMap(
					section -> new Tuple2<>(section.getId(), section)));
			dapController.getProfiles(sections.map(Section::getId))
			.onSuccess(profiles -> {
				mapPresenter.setLoadingState(false);
				profiles.filter(profile -> profile.getVendors()
							.contains(configuration.isBudokopProfiles() ? "budokop" : "neosentio"))
						.map(Profile::getSectionId)
						.distinct()
						.forEach(sectionId -> {
							Section section = configuration.getSections().get(sectionId);
							mapPresenter.setFeatureStrokeColor(section,
									configuration.isBudokopProfiles() ? "#ff5538" : "#3880ff");
							mapPresenter.add(section);
						});
			})
			.onFailure(this::handleCommunicationErrors);
		})
		.onFailure(this::handleCommunicationErrors);
	}
}
