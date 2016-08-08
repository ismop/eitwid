package pl.ismop.web.client.widgets.analysis.threatlevels;

import java.util.List;

import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.dap.scenario.Scenario;
import pl.ismop.web.client.widgets.analysis.threatlevels.IThreatLevelsView.IThreadLevelsPresenter;
import pl.ismop.web.client.widgets.analysis.threatlevels.scenario.ScenarioView;

public class ThreatLevelsView extends Composite implements IThreatLevelsView, ReverseViewInterface<IThreadLevelsPresenter> {
	interface ThreatLevelsViewUiBinder extends UiBinder<Widget, ThreatLevelsView> {
    }

    private static ThreatLevelsViewUiBinder uiBinder = GWT.create(ThreatLevelsViewUiBinder.class);


	private IThreadLevelsPresenter presenter;	

	@UiField
	FlowPanel scenarios;
	
	@UiField
	Row loading;
	
	@UiField
	InlineLabel loadingLabel;
	
	@UiField
	Div results;
	
	@UiField
	Select profilesSelect;
	
    public ThreatLevelsView() {
        initWidget(uiBinder.createAndBindUi(this));
        
        profilesSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				getPresenter().changeProfile(profilesSelect.getValue());
			}
		});
    }    
	
	@Override
	public void setPresenter(IThreadLevelsPresenter presenter) {
		this.presenter = presenter;		
	}

	@Override
	public IThreadLevelsPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void clearScenarios() {
		scenarios.clear();		
	}	
	
	@Override
	public void loading(String msg) {		
		loadingLabel.setText(msg);
		results.setVisible(false);
		loading.setVisible(true);
	}
	
	@Override
	public void showResults() {
		loading.setVisible(false);
		results.setVisible(true);
	}
	
	@Override
	public void setProfiles(List<String> names) {
		for(String name : names) {
			Option o = new Option();
			o.setText(name);
			profilesSelect.add(o);
		}
		profilesSelect.refresh();
		if(names.size() > 0) {
			getPresenter().changeProfile(names.get(0));
		}
	}

	@Override
	public void showScenarios(List<Scenario> scenarios) {
		int index = 1;
		for(Scenario scenario : scenarios) {
			this.scenarios.add(new ScenarioView(index++, scenario));
		}
	}
}
