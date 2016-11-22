package pl.ismop.web.client.widgets.analysis.verticalslice.wizard;

import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IVerticalSliceWizardView extends IsWidget {
	interface IVerticalSliceWizardPresenter {
		void onModalShown();

		void onModalHide();

		void onParameterChanged(String parameterName);

		void onAcceptConfig();

		void onDataSelectorChanged(String dataSelector);
	}

	void showModal(boolean show);

	void setMap(IsWidget view);

	void setProfile(String profileId, String string);

	void showLoadingState(boolean show);

	void removeParameter(String parameterName);

	void addParameter(String parameterName, boolean check, boolean enabled);

	void showNoParamtersLabel(boolean show);

	String getFullPanelTitle();

	void clearParameters();

	void clearProfiles();

	void showButtonConfigLabel(boolean show);

	String getRealDataLabel();

	void addScenarios(Map<String, String> scenariosMap);

	String getScenarioNamePrefix();

	void selectScenario(String dataSelector);

	String noProfilePickedMessage();
}