package pl.ismop.web.client.widgets.sideprofile;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.widgets.profile.IProfileView.ISideProfilePresenter;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = SideProfileView.class)
public class SideProfilePresenter extends BasePresenter<ISideProfileView, MainEventBus> implements ISideProfilePresenter {
	public void onStart() {
		eventBus.setTitleAndShow("Profile view", view);
		view.setScene(new SideProfileScene());
	}
}