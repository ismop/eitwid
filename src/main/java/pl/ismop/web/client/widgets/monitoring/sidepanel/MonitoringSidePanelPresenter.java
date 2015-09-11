package pl.ismop.web.client.widgets.monitoring.sidepanel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.monitoring.sidepanel.IMonitoringSidePanel.IMonitoringSidePanelPresenter;

@Presenter(view = MonitoringSidePanelView.class, multiple = true)
public class MonitoringSidePanelPresenter extends BasePresenter<IMonitoringSidePanel, MainEventBus> implements IMonitoringSidePanelPresenter {
	private DapController dapController;
	private Levee selectedLevee;

	@Inject
	public MonitoringSidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onLeveeNavigatorReady() {
		if(selectedLevee != null) {
			eventBus.leveeSelected(selectedLevee);
		}
	}
	
	public void onShowProfileMetadata(Profile profile, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getInternalIdLabel(), profile.getId());
		}
	}
	
	public void onShowSectionMetadata(Section section, boolean show) {
		view.clearMetadata();
		
		if(show) {
			view.addMetadata(view.getInternalIdLabel(), section.getId());
			view.addMetadata(view.getNameLabel(), section.getName());
		}
	}
	
	public void reset() {
		view.showLeveeName(false);
		view.showLeveeList(false);
		view.showLeveeProgress(true);
		loadLevees();
	}

	@Override
	public void handleShowFibreClick() {
		eventBus.showFibrePanel();
	}

	@Override
	public void handleShowWeatherClick() {
		eventBus.showWeatherPanel();
	}

	private void loadLevees() {
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				view.showLeveeProgress(false);
				eventBus.showError(errorDetails);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				view.showLeveeProgress(false);
				
				if(levees.size() > 0) {
					if(levees.size() == 1) {
						view.setLeveeName(levees.get(0).getName());
						view.showLeveeName(true);
					} else {
						Collections.sort(levees, new Comparator<Levee>() {
							@Override
							public int compare(Levee o1, Levee o2) {
								return o1.getName().compareTo(o2.getName());
							}
						});
						
						for(Levee levee : levees) {
							view.addLeveeOption(levee.getId(), levee.getName());
						}
					}
					
					selectedLevee = levees.get(0);
					eventBus.leveeSelected(selectedLevee);
				} else {
					view.showNoLeveesMessage();
				}
			}
		});
	}
}