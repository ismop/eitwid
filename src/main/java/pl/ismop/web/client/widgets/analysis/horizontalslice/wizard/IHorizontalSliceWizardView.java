package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import com.google.gwt.user.client.ui.IsWidget;

public interface IHorizontalSliceWizardView extends IsWidget {
	interface IHorizontalSliceWizardPresenter {
		void onModalShown();

		void onModalHide();
	}

	void showModal(boolean show);

	void setMap(IsWidget view);

	void showLoadingState(boolean show, String profileId);

	void addProfile(String profileId);
}