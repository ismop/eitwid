package pl.ismop.web.client.widgets.analysis.threatlevels.scenario;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.PanelType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import pl.ismop.web.client.dap.threatlevel.Scenario;


public class ScenarioView extends Composite {
    interface SensorPanelViewUiBinder extends UiBinder<Widget, ScenarioView> {
    }

    private static SensorPanelViewUiBinder uiBinder = GWT.create(SensorPanelViewUiBinder.class);

    @UiField
    Panel panel;

    @UiField
    Heading heading;

    @UiField
    HTML description;

    public ScenarioView(int rankingIndex, Scenario scenario) {
        initWidget(uiBinder.createAndBindUi(this));

        heading.setText(rankingIndex + ". " + scenario.getName());
        description.setText(scenario.getDescription());
        panel.setType(getType(scenario));
    }

	private PanelType getType(Scenario scenario) {
		PanelType type;
		switch(scenario.getThreatLevel()) {
			case 0:  type = PanelType.SUCCESS;
					 break;
			case 1:  type = PanelType.WARNING;
					 break;
			case 2:  type = PanelType.DANGER;
					 break;
			default: type = PanelType.DEFAULT;
					 break;
		}
		return type;
	}
}
