package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceWizardView extends IsWidget {
	interface IHorizontalSliceWizardPresenter {
		void onModalShown();

		void onModalHide();

		void onRemoveSection(String profileId);

		void onAcceptConfig();

		void onChangePickedHeight(String profileId, String height);

		void onParameterChanged(String parameterName);

		void onDataSelectorChanged(String dataSelector);

		void onProfileTypeChange(boolean budokopType);
	}

	void showModal(boolean show);

	void setMap(IsWidget view);

	void addPickedSection(String sectionId);

	void clearSections();

	void showLoadingState(boolean show, String sectionId);

	void removeSection(String sectionId);

	void addSectionHeight(Double height, String profileId, boolean check);

	void addParameter(String parameterName, boolean check, boolean enabled);

	void removeParameter(String parameterName);

	void showNoParamtersLabel(boolean show);

	String getFullPanelTitle();

	void clearParameters();

	void showButtonConfigLabel(boolean show);

	String getRealDataLabel();

	String getScenarioNamePrefix();

	void addScenarios(Map<String, String> scenariosMap);

	void selectScenario(String dataSelector);

	String noSectionPickedError();

	String singleProfilePerSection();

	void setBudokopProfilesToggle(boolean budokopProfiles);
}
