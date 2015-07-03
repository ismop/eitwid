package pl.ismop.web.client.widgets.sidepanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.widgets.section.SectionPresenter;
import pl.ismop.web.client.widgets.sidepanel.ISidePanelView.ISidePanelPresenter;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

@Presenter(view = SidePanelView.class)
public class SidePanelPresenter extends BasePresenter<ISidePanelView, MainEventBus> implements ISidePanelPresenter {
	private DapController dapController;
	private LeveeSummaryPresenter leveeSummaryPresenter;
	private SectionPresenter sectionPresenter;
	private Map<String, Section> sections;

	@Inject
	public SidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
		sections = new HashMap<>();
	}
	
	public void onStart() {
		eventBus.setSidePanel(view);
		view.setLeveeBusyState(true);
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(int code, String message) {
				view.setLeveeBusyState(false);
				Window.alert(message);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				view.setLeveeBusyState(false);
				
				if(levees.size() > 0) {
					view.showLeveeList(true);
					
					for(Levee levee : levees) {
						view.addLeveeValue(levee.getId(), levee.getName());
					}
					
					loadLeveeStatus(levees.get(0));
					loadSections(levees.get(0).getId());
				} else {
					view.showNoLeveesLabel(true);
				}
			}
		});
	}
	
	@Override
	public void onSectionChanged(String sectionId) {
		loadSectionStatus(sections.get(sectionId));
	}

	private void loadLeveeStatus(Levee levee) {
		if(leveeSummaryPresenter != null) {
			leveeSummaryPresenter.stopUpdate();
			view.removeSummaryView();
			eventBus.removeHandler(leveeSummaryPresenter);
			leveeSummaryPresenter = null;
		}
		
		leveeSummaryPresenter = eventBus.addHandler(LeveeSummaryPresenter.class);
		view.setLeveeSummary(leveeSummaryPresenter.getView());
		leveeSummaryPresenter.setLevee(levee);
	}
	
	private void loadSections(String leveeId) {
		view.showSectionPanel(true);
		view.setSectionBusyState(true);
		dapController.getSections(leveeId, new SectionsCallback() {
			@Override
			public void onError(int code, String message) {
				view.setSectionBusyState(false);
			}
			
			@Override
			public void processSections(List<Section> sections) {
				SidePanelPresenter.this.sections.clear();
				view.setSectionBusyState(false);
				
				if(sections.size() > 0) {
					view.showSectionList(true);
					view.addSectionValue(null, view.getPickSectionLabel());
					
					for(Section section : sections) {
						view.addSectionValue(section.getId(), section.getId());
						SidePanelPresenter.this.sections.put(section.getId(), section);
					}
				} else {
					view.showNoSectionsLabel(true);
				}
			}
		});
	}

	private void loadSectionStatus(Section section) {
		if(sectionPresenter != null) {
			sectionPresenter.stopUpdate();
			view.removeSectionView();
			eventBus.removeHandler(sectionPresenter);
			sectionPresenter = null;
		}
		
		if(section != null) {
			sectionPresenter = eventBus.addHandler(SectionPresenter.class);
			view.setSectionView(sectionPresenter.getView());
			sectionPresenter.setSection(section);
		}
	}
}