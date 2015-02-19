package pl.ismop.web.client.widgets.profile;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.widgets.profile.IProfileView.IProfilePresenter;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = ProfileView.class, multiple = true)
public class ProfilePresenter extends BasePresenter<IProfileView, MainEventBus> implements IProfilePresenter {
	private Section profile;

	public void setProfile(Section profile) {
		this.profile = profile;
		showProfileDetails();
	}

	public void stopUpdate() {
		// TODO Auto-generated method stub
		
	}

	public Section getProfile() {
		return profile;
	}
	
	private void showProfileDetails() {
		view.setHeader(profile.getName());
		view.setModeStyle(getPanelStyle(profile.getThreatLevel()));
		view.setThreatLevel(profile.getThreatLevel());
	}
	
	private String getPanelStyle(String level) {
		switch(level) {
			case "none":
				return "panel-info";
			case "heightened":
				return "panel-warning";
			case "severe":
				return "panel-danger";
			default:
				return "panel-default";
		}
	}
}