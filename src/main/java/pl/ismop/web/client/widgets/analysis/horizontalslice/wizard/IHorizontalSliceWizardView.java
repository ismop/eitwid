package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceWizardView extends IsWidget {
	interface IHorizontalSliceWizardPresenter {
		void onModalShown();

		void onModalHide();

		void onRemoveProfile(String profileId);
	}

	void showModal(boolean show);

	void setMap(IsWidget view);

	void addProfile(String profileId);

	void clearProfiles();

	void showLoadingState(boolean show, String profileId);

	void removeProfile(String profileId);

	void addProfileHeight(Double height, String profileId, boolean check);

	void showNoProfileLabel();
}