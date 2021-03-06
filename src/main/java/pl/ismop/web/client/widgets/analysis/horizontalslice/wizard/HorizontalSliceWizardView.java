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
import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;

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

	private Map<String, HasWidgets> sectionHeightsContainers;

	private Map<String, IsWidget> sectionWidgets, parameterWidgets;

	private ListBox scenarioList;

	@UiField
	HorizontalSliceWizardMessages messages;

	@UiField
	Modal modal;

	@UiField
	FlowPanel mapContainer, sections, parameters;

	@UiField
	Label noParameters;

	@UiField
	Button add;

	@UiField
	ToggleSwitch budokopToggle;

	public HorizontalSliceWizardView() {
		initWidget(uiBinder.createAndBindUi(this));
		sectionHeightsContainers = new HashMap<>();
		sectionWidgets = new HashMap<>();
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

	@UiHandler("budokopToggle")
	void budokopToggleSwitched(ValueChangeEvent<Boolean> event) {
		getPresenter().onProfileTypeChange(!event.getValue());
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
	public void addPickedSection(String sectionId) {
		Heading heading = new Heading(H4);
		heading.setText("Section " + sectionId);
		heading.addStyleName("pull-left");

		Button removeButton = new Button("", REMOVE, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getPresenter().onRemoveSection(sectionId);
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
		sectionHeightsContainers.put(sectionId, profileHeights);

		FlowPanel container = new FlowPanel();
		container.add(controls);
		container.add(profileHeights);
		sections.add(container);
		sectionWidgets.put(sectionId, container);
	}

	@Override
	public void clearSections() {
		sections.clear();
		sectionHeightsContainers.clear();
		sectionWidgets.clear();
	}

	@Override
	public void showLoadingState(boolean show, String profileId) {
		if(sectionHeightsContainers.get(profileId) != null) {
			if(show) {
				Icon icon = new Icon(CIRCLE_O_NOTCH);
				icon.setSpin(true);
				sectionHeightsContainers.get(profileId).add(icon);
			} else {
				sectionHeightsContainers.get(profileId).clear();
			}
		}
	}

	@Override
	public void removeSection(String sectionId) {
		sectionHeightsContainers.remove(sectionId);
		sections.remove(sectionWidgets.remove(sectionId));
	}

	@Override
	public void addSectionHeight(final Double height, final String sectionId, boolean check) {
		Radio radio = new Radio(sectionId, messages.heightLabel() + " "
				+ String.valueOf(NumberFormat.getFormat("0.00").format(height)));
		radio.setValue(check);
		radio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue()) {
					getPresenter().onChangePickedHeight(sectionId, String.valueOf(height));
				}
			}
		});
		sectionHeightsContainers.get(sectionId).add(radio);
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

		if (parameters.getWidgetCount() == 1 && scenarioList != null) {
			scenarioList.removeFromParent();
		}

		if (parameters.getWidgetCount() == 0) {
			showNoParamtersLabel(true);
		}
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
		scenarioList = null;
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
		if (scenarioList != null) {
			scenarioList.removeFromParent();
		}

		scenarioList = new ListBox();
		scenarioList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				getPresenter().onScenarioIdChanged(scenarioList.getSelectedValue());
			}
		});

		for(String id : scenariosMap.keySet()) {
			scenarioList.addItem(scenariosMap.get(id), id);
		}

		parameters.add(scenarioList);
	}

	@Override
	public void selectScenario(String scenarioId) {
		for(int i = 0; i < scenarioList.getItemCount(); i++) {
			if(scenarioList.getValue(i).equals(scenarioId)) {
				scenarioList.setSelectedIndex(i);

				break;
			}
		}
	}

	@Override
	public String noSectionPickedError() {
		return messages.noSectionPickedError();
	}

	@Override
	public String singleProfilePerSection() {
		return messages.singleProfilePerSection();
	}

	@Override
	public void setBudokopProfilesToggle(boolean budokopProfiles) {
		budokopToggle.setValue(!budokopProfiles);
	}
}
