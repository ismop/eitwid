package pl.ismop.web.client.widgets.section;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.widgets.section.ISectionView.ISectionPresenter;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

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
		view.setModeStyle(getPanelStyle(section.getThreatLevel()));
		view.setThreatLevel(section.getThreatLevel());
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