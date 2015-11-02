package pl.ismop.web.client.widgets.analysis.verticalslice;

import java.util.Date;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.widgets.analysis.verticalslice.IVerticalSliceView.IVerticalSlicePresenter;
import pl.ismop.web.client.widgets.common.panel.IPanelContent;
import pl.ismop.web.client.widgets.common.panel.ISelectionManager;

@Presenter(view = VerticalSliceView.class, multiple = true)
public class VerticalSlicePresenter extends BasePresenter<IVerticalSliceView, MainEventBus> implements IVerticalSlicePresenter, 
		IPanelContent<IVerticalSliceView, MainEventBus> {
	private DapController dapController;

	@Inject
	public VerticalSlicePresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void setConfiguration(VerticalCrosssectionConfiguration configuration) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelectedExperiment(Experiment experiment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelectedDate(Date date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelectionManager(ISelectionManager selectionManager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}