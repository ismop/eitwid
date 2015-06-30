package pl.ismop.web.client.widgets.sidepanel;

import java.util.List;

import javax.inject.Inject;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.sidepanel.ISidePanelView.ISidePanelPresenter;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = SidePanelView.class)
public class SidePanelPresenter extends BasePresenter<ISidePanelView, MainEventBus> implements ISidePanelPresenter {
	private DapController dapController;
	private LeveeSummaryPresenter leveeSummaryPresenter;

	@Inject
	public SidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onStart() {
		eventBus.setSidePanel(view);
		view.setLeveeBusyState(true);
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(int code, String message) {
				view.setLeveeBusyState(false);
				Window.alert(message);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				view.setLeveeBusyState(false);
				
				if(levees.size() > 0) {
					view.showLeveeList(true);
					
					for(Levee levee : levees) {
						view.addLeveeValue(levee.getId(), levee.getName());
					}
					
					loadLeveeStatus(levees.get(0));
				} else {
					view.showNoLeveesLabel(true);
				}
			}
		});
	}
	
	private void loadLeveeStatus(Levee levee) {
		if(leveeSummaryPresenter != null) {
			leveeSummaryPresenter.stopUpdate();
			view.removeSummaryView();
			eventBus.removeHandler(leveeSummaryPresenter);
			leveeSummaryPresenter = null;
		}
		
		leveeSummaryPresenter = eventBus.addHandler(LeveeSummaryPresenter.class);
		view.addLeveeSummary(leveeSummaryPresenter.getView());
		leveeSummaryPresenter.setLevee(levee);
	}
}