package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import static org.gwtbootstrap3.client.ui.constants.ButtonSize.EXTRA_SMALL;
import static org.gwtbootstrap3.client.ui.constants.ButtonType.DANGER;
import static org.gwtbootstrap3.client.ui.constants.HeadingSize.H4;
import static org.gwtbootstrap3.client.ui.constants.IconType.CIRCLE_O_NOTCH;
import static org.gwtbootstrap3.client.ui.constants.IconType.REMOVE;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Radio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.IHorizontalSliceWizardView.IHorizontalSliceWizardPresenter;

public class HorizontalSliceWizardView extends Composite implements IHorizontalSliceWizardView, ReverseViewInterface<IHorizontalSliceWizardPresenter> {
	private static HorizontalSliceWizardViewUiBinder uiBinder = GWT.create(HorizontalSliceWizardViewUiBinder.class);

	interface HorizontalSliceWizardViewUiBinder extends UiBinder<Widget, HorizontalSliceWizardView> {}

	private IHorizontalSliceWizardPresenter presenter;

	private Map<String, HasWidgets> profileHeightsContainers;

	private Map<String, IsWidget> profileWidgets, parameterWidgets;

	private ListBox scenarioList;

	@UiField
	HorizontalSliceWizardMessages messages;

	@UiField
	Modal modal;

	@UiField
	FlowPanel mapContainer, profiles, parameters;

	@UiField
	Label noProfilesPicked, noParameters;

	@UiField
	Button add;

	public HorizontalSliceWizardView() {
		initWidget(uiBinder.createAndBindUi(this));
		profileHeightsContainers = new HashMap<>();
		profileWidgets = new HashMap<>();
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
	public void addProfile(final String profileId, final String profileName) {
		noProfilesPicked.setVisible(false);

		Heading heading = new Heading(H4);
		heading.setText(profileName);
		heading.addStyleName("pull-left");

		Button removeButton = new Button("", REMOVE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getPresenter().onRemoveProfile(profileId);
			}
		});
		removeButton.addStyleName("pull-right");
		removeButton.setSize(EXTRA_SMALL);
		removeButton.setType(DANGER);

		FlowPanel controls = new FlowPanel();
		controls.addStyleName("clearfix");
		controls.add(heading);
		controls.add(removeButton);

		FlowPanel profileHeights = new FlowPanel();
		profileHeightsContainers.put(profileId, profileHeights);

		FlowPanel container = new FlowPanel();
		container.add(controls);
		container.add(profileHeights);
		profiles.add(container);
		profileWidgets.put(profileId, container);
	}

	@Override
	public void clearProfiles() {
		profiles.clear();
		profileHeightsContainers.clear();
		profileWidgets.clear();
		noProfilesPicked.setVisible(true);
	}

	@Override
	public void showLoadingState(boolean show, String profileId) {
		if(profileHeightsContainers.get(profileId) != null) {
			if(show) {
				Icon icon = new Icon(CIRCLE_O_NOTCH);
				icon.setSpin(true);
				profileHeightsContainers.get(profileId).add(icon);
			} else {
				profileHeightsContainers.get(profileId).clear();
			}
		}
	}

	@Override
	public void removeProfile(String profileId) {
		profileHeightsContainers.remove(profileId);
		profiles.remove(profileWidgets.remove(profileId));
	}

	@Override
	public void addProfileHeight(final Double height, final String profileId, boolean check) {
		Radio radio = new Radio(profileId, messages.heightLabel() + " " + String.valueOf(NumberFormat.getFormat("0.00").format(height)));
		radio.setValue(check);
		radio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue()) {
					getPresenter().onChangePickedHeight(profileId, String.valueOf(height));
				}
			}
		});
		profileHeightsContainers.get(profileId).add(radio);
	}

	@Override
	public void showNoProfileLabel() {
		noProfilesPicked.setVisible(true);
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
	public void removeParameter(String parameterName) {
		parameters.remove(parameterWidgets.remove(parameterName));
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
		parameterWidgets.clear();
		noParameters.setVisible(true);
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
	public String getRealDataLabel() {
		return messages.realDataLabel();
	}

	@Override
	public String getScenarioNamePrefix() {
		return messages.scenarioNamePrefix();
	}

	@Override
	public void addScenarios(Map<String, String> scenariosMap) {
		if(scenarioList != null) {
			scenarioList.removeFromParent();
		}

		scenarioList = new ListBox();
		scenarioList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				getPresenter().onDataSelectorChanged(scenarioList.getSelectedValue());
			}
		});

		for(String id : scenariosMap.keySet()) {
			scenarioList.addItem(scenariosMap.get(id), id);
		}

		parameters.add(scenarioList);
	}

	@Override
	public void selectScenario(String dataSelector) {
		for(int i = 0; i < scenarioList.getItemCount(); i++) {
			if(scenarioList.getValue(i).equals(dataSelector)) {
				scenarioList.setSelectedIndex(i);

				break;
			}
		}
	}

	@Override
	public String noProfilePickedError() {
		return messages.noProfilePickedError();
	}

	@Override
	public String singleProfilePerSection() {
		return messages.singleProfilePerSection();
	}
}