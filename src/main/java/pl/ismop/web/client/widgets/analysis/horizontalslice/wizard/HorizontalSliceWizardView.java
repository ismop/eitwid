package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.IHorizontalSliceWizardView.IHorizontalSliceWizardPresenter;

public class HorizontalSliceWizardView extends Composite implements IHorizontalSliceWizardView, ReverseViewInterface<IHorizontalSliceWizardPresenter> {
	private static HorizontalSliceWizardViewUiBinder uiBinder = GWT.create(HorizontalSliceWizardViewUiBinder.class);

	interface HorizontalSliceWizardViewUiBinder extends UiBinder<Widget, HorizontalSliceWizardView> {}
	
	private IHorizontalSliceWizardPresenter presenter;
	
	@UiField
	Modal modal;
	
	@UiField
	FlowPanel mapContainer, profiles;

	public HorizontalSliceWizardView() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("modal")
	void modalShown(ModalShownEvent event) {
		getPresenter().onModalShown();
	}
	
	@UiHandler("modal")
	void modalHide(ModalHideEvent event) {
		getPresenter().onModalHide();
	}

	@Override
	public void showModal(boolean show) {
		if(show) {
			modal.show();
		} else {
			modal.hide();
		}
	}

	@Override
	public void setPresenter(IHorizontalSliceWizardPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IHorizontalSliceWizardPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setMap(IsWidget view) {
		mapContainer.add(view);
	}

	@Override
	public void showLoadingState(boolean show, String profileId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addProfile(String profileId) {
		Heading heading = new Heading(HeadingSize.H5);
		heading.setText("Profile " + profileId);
		
		FlowPanel container = new FlowPanel();
		container.add(heading);
		profiles.add(container);
	}
}