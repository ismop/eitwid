package pl.ismop.web.client.widgets.analysis.horizontalslice.wizard;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.horizontalslice.wizard.IHorizontalSliceWizardView.IHorizontalSliceWizardPresenter;

@Presenter(view = HorizontalSliceWizardView.class)
public class HorizontalSliceWizardPresenter extends BasePresenter<IHorizontalSliceWizardView, MainEventBus> implements IHorizontalSliceWizardPresenter {
	public void onShowHorizontalCrosssectionWizard() {
		view.showModal(true);
	}
}