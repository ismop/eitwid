package pl.ismop.web.client.widgets.analysis.verticalslice.wizard;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.verticalslice.wizard.IVerticalSliceWizardView.IVerticalSliceWizardPresenter;

@Presenter(view = VerticalSliceWizardView.class)
public class VerticalSliceWizardPresenter extends BasePresenter<IVerticalSliceWizardView, MainEventBus> implements IVerticalSliceWizardPresenter {
	@Inject
	public VerticalSliceWizardPresenter() {
		
	}
	
	public void onShowVerticalCrosssectionWizard() {
		view.showModal(true);
	}
}