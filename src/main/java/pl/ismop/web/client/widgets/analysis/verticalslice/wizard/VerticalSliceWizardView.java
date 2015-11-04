package pl.ismop.web.client.widgets.analysis.verticalslice.wizard;

import static org.gwtbootstrap3.client.ui.constants.HeadingSize.H4;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.analysis.verticalslice.wizard.IVerticalSliceWizardView.IVerticalSliceWizardPresenter;

public class VerticalSliceWizardView extends Composite implements IVerticalSliceWizardView, ReverseViewInterface<IVerticalSliceWizardPresenter> {
	private static VerticalSliceWizardViewUiBinder uiBinder = GWT.create(VerticalSliceWizardViewUiBinder.class);

	interface VerticalSliceWizardViewUiBinder extends UiBinder<Widget, VerticalSliceWizardView> {}
	
	private IVerticalSliceWizardPresenter presenter;
	
	private Map<String, IsWidget> parameterWidgets;

	@UiField
	VerticalSliceWizardMessages messages;
	
	@UiField
	Modal modal;
	
	@UiField
	FlowPanel mapContainer, profiles, loadingPanel, parameters;
	
	@UiField
	Label noProfilesPicked, noParameters;
	
	@UiField
	Button add;
	
	public VerticalSliceWizardView() {
		initWidget(uiBinder.createAndBindUi(this));
		parameterWidgets = new HashMap<>();
	}
	
	@UiHandler("modal")
	void modalShown(ModalShownEvent event) {
		getPresenter().onModalShown();
	}
	
	@UiHandler("modal")
	void modalHide(ModalHideEvent event) {
		getPresenter().onModalHide();
	}
	
	@UiHandler("add")
	void addPanel(ClickEvent event) {
		getPresenter().onAcceptConfig();
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
	public void setPresenter(IVerticalSliceWizardPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IVerticalSliceWizardPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setMap(IsWidget view) {
		mapContainer.add(view);
	}

	@Override
	public void setProfile(String profileId) {
		noProfilesPicked.setVisible(false);
		profiles.clear();
		
		Heading heading = new Heading(H4);
		heading.setText("Profile " + profileId);
		heading.addStyleName("pull-left");
		
		FlowPanel container = new FlowPanel();
		container.add(heading);
		profiles.add(container);
	}

	@Override
	public void showLoadingState(boolean show) {
		loadingPanel.setVisible(show);
	}

	@Override
	public void removeParameter(String parameterName) {
		parameters.remove(parameterWidgets.remove(parameterName));
	}

	@Override
	public void addParameter(final String parameterName, boolean check, boolean enabled) {
		noParameters.setVisible(false);
		
		Radio radio = new Radio("parameters", parameterName);
		radio.setValue(check);
		radio.setEnabled(enabled);
		
		if(!enabled) {
			radio.setTitle(messages.parameterDisabledInfo());
		}
		
		radio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue()) {
					getPresenter().onParameterChanged(parameterName);
				}
			}
		});
		parameterWidgets.put(parameterName, radio);
		parameters.add(radio);
	}

	@Override
	public void showNoParamtersLabel(boolean show) {
		noParameters.setVisible(show);
	}

	@Override
	public String getFullPanelTitle() {
		return messages.fullPanelTitle();
	}

	@Override
	public void clearParameters() {
		parameters.clear();
	}

	@Override
	public void clearProfiles() {
		profiles.clear();
	}

	@Override
	public void showButtonConfigLabel(boolean show) {
		if(show) {
			add.setText(messages.updatePanelLabel());
		} else {
			add.setText(messages.addPanelLabel());
		}
	}

	@Override
	public void showNoProfilePickedError() {
		Window.alert(messages.noProfilePickedMessage());
	}
}