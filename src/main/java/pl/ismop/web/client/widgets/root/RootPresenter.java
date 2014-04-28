package pl.ismop.web.client.widgets.root;

import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class RootPresenter extends BaseEventHandler<MainEventBus> {
	private DapController dapController;

	@Inject
	public RootPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onStart() {
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				RootPanel leveesPanel = RootPanel.get("leveesContainer");
				
				if(leveesPanel != null) {
					for(Levee levee : levees) {
						LeveeSummaryPresenter presenter = eventBus.addHandler(LeveeSummaryPresenter.class);
						presenter.setLevee(levee);
						leveesPanel.add(presenter.getView());
					}
				}
			}
		});
	}
}