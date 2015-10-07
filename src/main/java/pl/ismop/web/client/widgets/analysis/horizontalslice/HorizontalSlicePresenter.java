package pl.ismop.web.client.widgets.analysis.horizontalslice;

import java.util.Date;
import java.util.Map;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.experiment.Experiment;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.widgets.analysis.comparison.IPanelContent;
import pl.ismop.web.client.widgets.analysis.horizontalslice.IHorizontalSliceView.IHorizontalSlicePresenter;

@Presenter(view = HorizontalSliceView.class, multiple = true)
public class HorizontalSlicePresenter extends BasePresenter<IHorizontalSliceView, MainEventBus> implements IHorizontalSlicePresenter,
	IPanelContent<IHorizontalSliceView, MainEventBus> {

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

	public void setProfileHeights(Map<Profile, String> profileHeights) {
		// TODO Auto-generated method stub
		
	}
}