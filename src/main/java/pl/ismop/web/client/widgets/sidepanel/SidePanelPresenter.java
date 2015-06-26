package pl.ismop.web.client.widgets.sidepanel;

import java.util.List;

import javax.inject.Inject;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.sidepanel.ISidePanelView.ISidePanelPresenter;

import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = SidePanelView.class)
public class SidePanelPresenter extends BasePresenter<ISidePanelView, MainEventBus> implements ISidePanelPresenter {
	private DapController dapController;

	@Inject
	public SidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onStart() {
		eventBus.setSidePanel(view);
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert(message);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				for(Levee levee : levees) {
					view.addLeveeValue(levee.getId(), levee.getName());
				}
			}
		});
	}
}