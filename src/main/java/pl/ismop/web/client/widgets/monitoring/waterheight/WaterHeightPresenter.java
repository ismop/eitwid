package pl.ismop.web.client.widgets.monitoring.waterheight;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.levee.Levee;

@Presenter(view = WaterHeightView.class)
public class WaterHeightPresenter extends BasePresenter<IWaterHeightView, MainEventBus> {

	private DapController dapController;

	// use Level2_PV sensor
	
	@Inject
	public WaterHeightPresenter(DapController dapController) {
		this.dapController = dapController;		
	}
	
	public void onShowWaterHightPanel(Levee selectedLevee) {
		view.showModal(true);		
	}
}
