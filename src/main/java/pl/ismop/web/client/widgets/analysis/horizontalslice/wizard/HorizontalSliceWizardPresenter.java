package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import static javaslang.API.Tuple;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import javaslang.Tuple;
import javaslang.collection.HashMap;
import javaslang.collection.LinkedHashMap;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.control.Option;
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

	private static final String BUDOKOP = "budokop";

	private static final String NEOSENTIO = "neosentio";

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
		configuration.setProfileVendor(BUDOKOP);
		view.setBudokopProfilesToggle(true);
		configuration.setExperiment(experiment);
		view.showModal(true);
	}

	public void onShowHorizontalCrosssectionWizardWithConfig(
			HorizontalCrosssectionConfiguration configuration) {
		onShowHorizontalCrosssectionWizard(experiment);
		this.configuration = configuration;
		configMode = true;
		initializeFromConfig();
	}

	@Override
	public void onModalShown() {
		if (mapPresenter == null) {
			mapPresenter = eventBus.addHandler(MapPresenter.class);
			mapPresenter.addClickListeners();
			view.setMap(mapPresenter.getView());
		}

		if (!configMode) {
			retrieveAndShowSections();
		}
	}

	@Override
	public void onModalHide() {
		eventBus.horizontalCrosssectionWizardHidden();
	}

	public void onSectionClicked(Section section) {
		if (!configuration.getPickedSectionIds().contains(section.getId())) {
			view.addPickedSection(section.getId());
			configuration.setPickedSectionIds(
					configuration.getPickedSectionIds().append(section.getId()));
			view.showLoadingState(true, section.getId());
			configuration.getDevicesBySectionId().get(section.getId()).peek(devices -> {
				computeHeights(devices, section.getId());
				dapController.getParameters(devices.map(Device::getId)).onSuccess(parameters -> {

				});
			});




			dapController.getProfiles(javaslang.collection.List.of(section.getId()))
				.flatMap(profiles -> dapController.getAllDevicesForProfiles(
						profiles
							.filter(profile -> profile.getVendors()
									.contains(configuration.getProfileVendor()))
							.map(Profile::getId)))
				.onSuccess(devices -> {
					configuration.setDevicesBySectionId(
							configuration.getDevicesBySectionId().put(section.getId(), devices));
					dapController.getParameters(devices.map(Device::getId))
					.onSuccess(parameters -> {
						dapController.getExperimentScenarios(experiment.getId())
						.onSuccess(scenarios -> {
							view.showLoadingState(false, section.getId());

//							for(Parameter parameter : parameters) {
//								configuration.getParameterMap().put(parameter.getId(), parameter);
//							}
//
//							for(Scenario scenario : scenarios) {
//								configuration.getScenarioMap().put(scenario.getId(), scenario);
//							}

							computeHeights(devices, section.getId());
							updateParametersAndScenarios(parameters, scenarios);
						})
						.onFailure(e -> handleCommunicationErrors(e, section));
					})
					.onFailure(e -> handleCommunicationErrors(e, section));
				})
				.onFailure(e -> handleCommunicationErrors(e, section));
		}
	}

	@Override
	public void onRemoveSection(String sectionId) {
		if(configuration.getPickedSectionIds().contains(sectionId)) {
			configuration.setPickedSectionIds(
					configuration.getPickedSectionIds().remove(sectionId));

			Option<String> pickedHeight = configuration
					.getPickedHeightsBySectionId().get(sectionId);

			if (pickedHeight.isDefined()) {
				configuration.setPickedHeightsBySectionId(
						configuration.getPickedHeightsBySectionId().remove(sectionId));
				configuration.setDevicesBySectionIdAndHeight(
						configuration.getDevicesBySectionIdAndHeight().remove(sectionId));
			}

			view.removeSection(sectionId);

			//updating parameter list
//			List<Parameter> parameters = new ArrayList<>();
//
//			for(Section profile : configuration.getPickedSections().values()) {
//				for(Device device : configuration.getSectionDevicesMap().get(profile)) {
//					for(String parameterId : device.getParameterIds()) {
//						parameters.add(configuration.getParameterMap().get(parameterId));
//					}
//				}
//			}

//			updateParametersAndScenarios(parameters, new ArrayList<>(
//					configuration.getScenarioMap().values()));
		}
	}

	@Override
	public void onAcceptConfig() {
		if (configuration.getPickedSectionIds().size() == 0
				|| configuration.getPickedParameterName() == null) {
			eventBus.showSimpleError(view.noSectionPickedError());
		} else {
			view.showModal(false);

			if (configMode) {
				eventBus.updateHorizontalSliceConfiguration(configuration);
			} else {
				HorizontalSlicePresenter horizontalSlicePresenter = eventBus.addHandler(
						HorizontalSlicePresenter.class);
				horizontalSlicePresenter.setConfiguration(configuration);
				eventBus.addPanel(view.getFullPanelTitle(), horizontalSlicePresenter);
			}
		}
	}

	@Override
	public void onChangePickedHeight(String sectionId, String height) {
		configuration.setPickedHeightsBySectionId(
				configuration.getPickedHeightsBySectionId().put(sectionId, height));
	}

	@Override
	public void onParameterChanged(String parameterName) {
		configuration.setPickedParameterName(parameterName);
	}

	@Override
	public void onDataSelectorChanged(String dataSelector) {
//		configuration.setDataSelector(dataSelector);
	}

	@Override
	public void onProfileTypeChange(boolean budokopType) {
		this.configuration.setProfileVendor(budokopType ? BUDOKOP : NEOSENTIO);
		retrieveAndShowSections();
	}

	private void initializeFromConfig() {
		view.showButtonConfigLabel(true);
		view.setBudokopProfilesToggle(configuration.getProfileVendor().equals(BUDOKOP));
		drawSections();

//		Map<String, Integer> counter = countParameters(configuration.getParameterMap().values());
//
//		for (String parameterName : configuration.getParameterNames()) {
//			if (parameterName.equals(configuration.getPickedParameterMeasurementName())) {
//				view.addParameter(parameterName, true, counter.get(parameterName) > 1);
//			} else {
//				view.addParameter(parameterName, false, counter.get(parameterName) > 1);
//			}
//		}
//
//		for (String sectionId : configuration.getPickedSections().keySet()) {
//			view.addPickedSection(sectionId);
//
//			Section section = configuration.getPickedSections().get(sectionId);
//
//			for (String height : configuration.getSectionHeights().get(section)) {
//				view.addSectionHeight(Double.parseDouble(height), sectionId,
//						configuration.getPickedHeights().get(section).equals(height));
//			}
//		}
//
//		processScenarios(new ArrayList<>(configuration.getScenarioMap().values()));
//		view.selectScenario(configuration.getDataSelector());
	}

	private void computeHeights(Seq<Device> devices, String sectionId) {
		Seq<Device> sortedDevices = devices.filter(device -> device.getPlacement() != null
				&& device.getPlacement().getCoordinates().size() > 1)
		.sorted((o1, o2) -> o1.getPlacement().getCoordinates().get(2).compareTo(
				o2.getPlacement().getCoordinates().get(2)));
		sortedDevices.getOption()
				.map(device -> device.getPlacement().getCoordinates().get(2))
				.map(minHeight -> {
					return (Map<Double, Seq<Device>>) devices.groupBy(
							device -> (device.getPlacement().getCoordinates().get(2) - minHeight)
							/ HEIGHT_THRESHOLD + minHeight + 1);
				})
				.peek(heights -> {
					configuration.setHeightsBySectionId(configuration.getHeightsBySectionId()
							.put(sectionId, heights.keySet().toList()
									.map(height -> String.valueOf(height))));
					configuration.setPickedHeightsBySectionId(
							configuration.getPickedHeightsBySectionId()
							.put(sectionId, String.valueOf(
									heights.keySet().toList().sorted().get())));
					configuration.setDevicesBySectionIdAndHeight(
							configuration.getDevicesBySectionIdAndHeight()
							.put(sectionId, heights.map((height, heightDevices) ->
									Tuple(String.valueOf(height), heightDevices))));
					heights.keySet().toList().sorted().zipWithIndex().forEach(indexedHeight -> {
						boolean first = indexedHeight._2() == 0;
						view.addSectionHeight(indexedHeight._1(), sectionId, first);
					});
				});
	}

	private void updateParametersAndScenarios(Seq<Parameter> parameters,
			Seq<Scenario> scenarios) {
//		configuration.setDataSelector("0");
//
//		Set<String> result = new HashSet<>();
//		Map<String, Integer> counter = countParameters(parameters);
//
//		for (Parameter parameter : parameters) {
//			result.add(parameter.getMeasurementTypeName());
//		}
//
//		for (Iterator<String> i = configuration.getParameterNames().iterator(); i.hasNext();) {
//			String parameterName = i.next();
//
//			if (!result.contains(parameterName)) {
//				i.remove();
//				configuration.getParameterNames().remove(parameterName);
//				view.removeParameter(parameterName);
//
//				if (parameterName.equals(configuration.getPickedParameterMeasurementName())) {
//					configuration.setPickedParameterMeasurementName(null);
//				}
//			}
//		}
//
//		for (String parameterName : result) {
//			if (!configuration.getParameterNames().contains(parameterName)) {
//				if (configuration.getPickedParameterMeasurementName() == null) {
//					view.addParameter(parameterName, true, counter.get(parameterName) > 1);
//					configuration.setPickedParameterMeasurementName(parameterName);
//				} else {
//					view.addParameter(parameterName, false, counter.get(parameterName) > 1);
//				}
//
//				configuration.getParameterNames().add(parameterName);
//			}
//		}
//
//		if (configuration.getParameterNames().size() == 0) {
//			view.showNoParamtersLabel(true);
//		} else {
//			processScenarios(scenarios);
//		}
//
//		if (configuration.getParameterNames().size() == 0) {
//			view.showNoParamtersLabel(true);
//		}
	}

	private void processScenarios(Seq<Scenario> scenarios) {
		Map<String, String> scenariosMap = LinkedHashMap.empty();
		scenariosMap.put("0", view.getRealDataLabel());

		for(Scenario scenario : scenarios) {
			scenariosMap.put(scenario.getId(), view.getScenarioNamePrefix() + " "
					+ scenario.getName());
		}

		view.addScenarios(scenariosMap.toJavaMap());
	}

	private Map<String, Integer> countParameters(Seq<Parameter> parameters) {
		Map<String, Integer> result = HashMap.empty();

		for(Parameter parameter : parameters) {
			result.put(parameter.getMeasurementTypeName(), result.get(
					parameter.getMeasurementTypeName()) == null ? 1
							: result.get(parameter.getMeasurementTypeName()).get() + 1);
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

	private void retrieveAndShowSections() {
		mapPresenter.reset(false);
		mapPresenter.setLoadingState(true);
		dapController.getSections()
		.flatMap(sections -> {
			configuration.setSections(
					sections.toMap(section -> Tuple.of(section.getId(), section)));

			return dapController.getProfiles(sections.map(Section::getId));
		})
		.map(profiles -> profiles.filter(
				profile -> profile.getVendors().contains(configuration.getProfileVendor())))
		.flatMap(vendorProfiles -> dapController.getAllDevicesForProfiles(
				vendorProfiles.map(Profile::getId)))
		.onSuccess(devices -> {
			configuration.setDevicesBySectionId((javaslang.collection.Map<String, Seq<Device>>)
					devices.groupBy(Device::getSectionId));
			drawSections();
			mapPresenter.setLoadingState(false);
		})
		.onFailure(this::handleCommunicationErrors);
	}

	private void drawSections() {
		configuration.getDevicesBySectionId().keySet()
			.map(vendorSectionid -> configuration.getSections().get(vendorSectionid))
			.filter(Option::isDefined)
			.map(Option::get)
			.forEach(section -> {
				mapPresenter.setFeatureStrokeColor(section,
						configuration.getProfileVendor().equals(BUDOKOP)
						? "#ff5538" : "#3880ff");
				mapPresenter.add(section);
			});
	}
}
