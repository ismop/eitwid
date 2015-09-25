package pl.ismop.web.client.widgets.analysis.sidepanel;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.analysis.sidepanel.IAnalysisSidePanel.IAnalysisSidePanelPresenter;

@Presenter(view = AnalysisSidePanel.class, multiple = true)
public class AnalysisSidePanelPresenter extends BasePresenter<IAnalysisSidePanel, MainEventBus> implements IAnalysisSidePanelPresenter {

}