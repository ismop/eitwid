package pl.ismop.web.client.widgets.levees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.levees.ILeveesView.ILeveesPresenter;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = LeveesView.class)
public class LeveesPresenter extends BasePresenter<ILeveesView, MainEventBus> implements ILeveesPresenter {
	private DapController dapController;
	private Map<String, LeveeSummaryPresenter> leveePresenters;

	@Inject
	public LeveesPresenter(DapController dapController) {
		this.dapController = dapController;
		leveePresenters = new HashMap<>();
	}
	
	public void onShowLeveeList() {
		eventBus.popupClosed();
		eventBus.setTitleAndShow(view.popupTitle(), view);
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				if(levees.size() == 0) {
					view.showNoLeveesMessage();
				} else {
					for(Levee levee : levees) {
						LeveeSummaryPresenter presenter = leveePresenters.get(levee.getId());
						
						if(presenter == null) {
							presenter = eventBus.addHandler(LeveeSummaryPresenter.class);
							view.addLeveeWidget(presenter.getView());
							leveePresenters.put(levee.getId(), presenter);
							presenter.setLevee(levee);
						}
					}
				}
			}
		});
	}
	
	public void onPopupClosed() {
		for(LeveeSummaryPresenter presenter : leveePresenters.values()) {
			presenter.stopUpdate();
		}
	}
}