package pl.ismop.web.client.widgets.analysis.comparison;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.comparison.IComparisonView.IComparisonPresenter;

@Presenter(view = ComparisonView.class, multiple = true)
public class ComparisonPresenter extends BasePresenter<IComparisonView, MainEventBus>implements IComparisonPresenter {

}