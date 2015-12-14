package pl.ismop.web.client.widgets.old.section;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.widgets.old.section.ISectionView.ISectionPresenter;

@Presenter(view = SectionView.class, multiple = true)
public class SectionPresenter extends BasePresenter<ISectionView, MainEventBus> implements ISectionPresenter {
	private Section section;

	public void setSection(Section section) {
		this.section = section;
		showSectionDetails();
	}

	public void stopUpdate() {
		
	}

	public Section getSection() {
		return section;
	}
	
	private void showSectionDetails() {
		view.setHeader(view.getHeaderLabel(section.getId()));
//		view.setModeStyle(getPanelStyle(section.getThreatLevel()));
//		view.setThreatLevel(section.getThreatLevel());
		//TODO(DH): check where the threat level state went
		view.setModeStyle("panel-default");
		view.setThreatLevel("TODO");
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