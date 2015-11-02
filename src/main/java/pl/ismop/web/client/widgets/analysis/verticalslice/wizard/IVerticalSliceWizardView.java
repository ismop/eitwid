package pl.ismop.web.client.widgets.analysis.verticalslice.wizard;

import com.google.gwt.user.client.ui.IsWidget;

public interface IVerticalSliceWizardView extends IsWidget {
	interface IVerticalSliceWizardPresenter {
		void onModalShown();

		void onModalHide();

		void onParameterChanged(String parameterName);

		void onAddPanel();
	}
	
	void showModal(boolean show);

	void setMap(IsWidget view);

	void setProfile(String profileId);

	void showLoadingState(boolean show);

	void removeParameter(String parameterName);

	void addParameter(String parameterName, boolean check);

	void showNoParamtersLabel(boolean show);

	String getFullPanelTitle();
}