package pl.ismop.web.client.widgets.analysis.horizontalslice;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.horizontalslice.IHorizontalSliceView.IHorizontalSlicePresenter;

@Presenter(view = HorizontalSliceView.class, multiple = true)
public class HorizontalSlicePresenter extends BasePresenter<IHorizontalSliceView, MainEventBus> implements IHorizontalSlicePresenter {
	
}