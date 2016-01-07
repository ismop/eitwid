package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceWizardView extends IsWidget {
	interface IHorizontalSliceWizardPresenter {
		void onModalShown();

		void onModalHide();

		void onRemoveProfile(String profileId);

		void onAcceptConfig();

		void onChangePickedHeight(String profileId, String height);

		void onParameterChanged(String parameterName);

		void onDataSelectorChanged(String dataSelector);
	}

	void showModal(boolean show);

	void setMap(IsWidget view);

	void addProfile(String profileId);

	void clearProfiles();

	void showLoadingState(boolean show, String profileId);

	void removeProfile(String profileId);

	void addProfileHeight(Double height, String profileId, boolean check);

	void showNoProfileLabel();

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

	String noProfilePickedError();

	String singleProfilePerSection();
}