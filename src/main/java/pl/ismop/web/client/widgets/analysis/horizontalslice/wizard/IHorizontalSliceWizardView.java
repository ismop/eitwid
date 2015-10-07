package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceWizardView extends IsWidget {
	interface IHorizontalSliceWizardPresenter {
		void onModalShown();

		void onModalHide();

		void onRemoveProfile(String profileId);

		void onAddPanel();

		void onChangePickedHeight(String profileId, String height);
	}

	void showModal(boolean show);

	void setMap(IsWidget view);

	void addProfile(String profileId);

	void clearProfiles();

	void showLoadingState(boolean show, String profileId);

	void removeProfile(String profileId);

	void addProfileHeight(Double height, String profileId, boolean check);

	void showNoProfileLabel();

	void addParameter(String parameterName, boolean check);

	void removeParameter(String parameterName);

	void showNoParamtersLabel(boolean show);

	void showNoProfilePickedError();

	String getFullPanelTitle();
}