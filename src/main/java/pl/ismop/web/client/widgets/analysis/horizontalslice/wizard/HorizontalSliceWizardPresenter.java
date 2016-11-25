package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import static javaslang.API.LinkedMap;
import static javaslang.API.Tuple;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import javaslang.Tuple;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.concurrent.Future;
import javaslang.control.Option;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.FunctionalDapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.parameter.Parameter;
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

	private static final Logger log = LoggerFactory.getLogger(HorizontalSliceWizardPresenter.class);

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

	@SuppressWarnings("unchecked")
	public void onSectionClicked(Section section) {
		if (!configuration.getPickedSectionIds().contains(section.getId())) {
			view.addPickedSection(section.getId());
			configuration.setPickedSectionIds(
					configuration.getPickedSectionIds().append(section.getId()));
			view.showLoadingState(true, section.getId());
			configuration.getDevicesBySectionId().get(section.getId()).peek(devices -> {
				Future.sequence(List.of(
						dapController.getParameters(devices.map(Device::getId)),
						dapController.getExperimentScenarios(experiment.getId())))
					.onSuccess(results -> {
						Seq<Parameter> parameters = (Seq<Parameter>) results.get(0);
						Seq<Scenario> scenarios = (Seq<Scenario>) results.get(1);
						view.showLoadingState(false, section.getId());
						computeAndDrawHeights(section.getId());
						configuration.setParametersById(configuration.getParametersById()
								.merge(parameters.toMap(
										parameter -> Tuple(parameter.getId(), parameter))));
						configuration.setScenariosById(configuration.getScenariosById()
								.merge(scenarios.toMap(
										scenario -> Tuple(scenario.getId(), scenario))));
						updateParametersAndScenarios();
					})
					.onFailure(e -> handleCommunicationErrors(e, section));
			});
		}
	}

	@Override
	public void onRemoveSection(String sectionId) {
		if (configuration.getPickedSectionIds().contains(sectionId)) {
			configuration.setPickedSectionIds(
					configuration.getPickedSectionIds().remove(sectionId));
			configuration.getPickedHeightsBySectionId().get(sectionId)
				.peek(height -> {
					configuration.setPickedHeightsBySectionId(
							configuration.getPickedHeightsBySectionId().remove(sectionId));
					configuration.setDevicesBySectionIdAndHeight(
							configuration.getDevicesBySectionIdAndHeight().remove(sectionId));
				});
			configuration.getDevicesBySectionId().get(sectionId).peek(devices -> {
				configuration.setParametersById(configuration.getParametersById()
						.removeAll(devices.flatMap(Device::getParameterIds)));
			});
			view.removeSection(sectionId);
			updateParametersAndScenarios();
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
	public void onScenarioIdChanged(String scenarioId) {
		configuration.setPickedScenarioId(scenarioId);
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
		configuration.getPickedSectionIds().forEach(pickedSectionId -> {
			view.addPickedSection(pickedSectionId);
			computeAndDrawHeights(pickedSectionId);
		});
		updateParametersAndScenarios();
		view.selectScenario(configuration.getPickedScenarioId());
	}

	@SuppressWarnings("unchecked")
	private void computeAndDrawHeights(String sectionId) {
		configuration.getDevicesBySectionId().get(sectionId)
			.peek(devices -> {
				devices.filter(device -> device.getPlacement() != null
						&& device.getPlacement().getCoordinates().size() > 1)
				.sorted((o1, o2) -> o1.getPlacement().getCoordinates().get(2).compareTo(
						o2.getPlacement().getCoordinates().get(2)))
				.getOption()
				.map(device -> device.getPlacement().getCoordinates().get(2))
				.map(minHeight -> {
					return (Map<Double, Seq<Device>>) devices.groupBy(
							device -> Math.round((device.getPlacement().getCoordinates().get(2)
									- minHeight) / HEIGHT_THRESHOLD) + minHeight + 1);
				})
				.peek(devicesByHeight -> {
					configuration.setHeightsBySectionId(configuration.getHeightsBySectionId()
							.put(sectionId, devicesByHeight.keySet().toList()
									.map(height -> String.valueOf(height))));

					if (!configuration.getPickedHeightsBySectionId().get(sectionId).isDefined()) {
						configuration.setPickedHeightsBySectionId(
								configuration.getPickedHeightsBySectionId()
								.put(sectionId, String.valueOf(
										devicesByHeight.keySet().toList().sorted().get())));
					}

					configuration.setDevicesBySectionIdAndHeight(
							configuration.getDevicesBySectionIdAndHeight()
							.put(sectionId, devicesByHeight.map((height, heightDevices) ->
							Tuple(String.valueOf(height), heightDevices))));
					devicesByHeight.keySet().toList().sorted().zipWithIndex()
						.forEach(indexedHeight -> {
							boolean checked = String.valueOf(indexedHeight._1())
									.equals(configuration.getPickedHeightsBySectionId()
											.get(sectionId).get());
							view.addSectionHeight(indexedHeight._1(), sectionId, checked);
						});
				});
			});
	}

	private void updateParametersAndScenarios() {
		if (configuration.getPickedScenarioId() == null) {
			configuration.setPickedScenarioId("0");
		}

		view.clearParameters();

		Map<String, Integer> parameterNameCounts = configuration.getParametersById().values()
				.map(Parameter::getMeasurementTypeName)
				.groupBy(parameterName -> parameterName)
				.map((parameterName, duplicates) -> Tuple(parameterName, duplicates.size()));

		if (parameterNameCounts.size() == 0) {
			view.showNoParamtersLabel(true);
		} else {
			parameterNameCounts.forEach((parameterName, parameterCount) -> {
				if (configuration.getPickedParameterName() == null) {
					view.addParameter(parameterName, true, parameterCount > 1);
					configuration.setPickedParameterName(parameterName);
				} else if (parameterName.equals(configuration.getPickedParameterName())) {
					view.addParameter(parameterName, true, parameterCount > 1);
				} else {
					view.addParameter(parameterName, false, parameterCount > 1);
				}
			});

			Map<String, String> scenarios = LinkedMap("0", view.getRealDataLabel())
					.merge(configuration.getScenariosById().map((scenarioId, scenario) ->
					Tuple(scenarioId, view.getScenarioNamePrefix() + " " + scenario.getName())));
			view.addScenarios(scenarios.toJavaMap());

			if (configuration.getPickedScenarioId() != null) {
				view.selectScenario(configuration.getPickedScenarioId());
			}
		}
	}

	private void handleCommunicationErrors(Throwable e, Section section) {
		view.showLoadingState(false, section.getId());
		eventBus.showError(errorUtil.processErrors(null, e));
	}

	private void handleCommunicationErrors(Throwable e) {
		mapPresenter.setLoadingState(false);
		eventBus.showError(errorUtil.processErrors(null, e));
	}

	@SuppressWarnings("unchecked")
	private void retrieveAndShowSections() {
		mapPresenter.reset(false);
		configuration.setDevicesBySectionIdAndHeight(HashMap.empty());
		configuration.setHeightsBySectionId(HashMap.empty());
		configuration.setParametersById(HashMap.empty());
		configuration.setPickedHeightsBySectionId(HashMap.empty());
		configuration.setPickedParameterName(null);
		configuration.setPickedScenarioId(null);
		configuration.setScenariosById(HashMap.empty());
		configuration.setPickedSectionIds(List.empty());
		view.clearSections();
		view.clearParameters();
		mapPresenter.setLoadingState(true);
		dapController.getSections()
		.flatMap(sections -> {
			configuration.setSections(
					sections.toMap(section -> Tuple.of(section.getId(), section)));

			return dapController.getProfiles(sections.map(section -> section.getId()));
		})
		.map(profiles -> profiles.filter(
				profile -> profile.getVendors().contains(configuration.getProfileVendor())))
		.flatMap(vendorProfiles -> dapController.getAllDevicesForProfiles(
				vendorProfiles.map(profile -> profile.getId())))
		.onSuccess(devices -> {
			mapPresenter.setLoadingState(false);
			configuration.setDevicesBySectionId((javaslang.collection.Map<String, Seq<Device>>)
					devices.groupBy(Device::getSectionId));
			drawSections();
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
