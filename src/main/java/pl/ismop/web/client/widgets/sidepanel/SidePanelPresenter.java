package pl.ismop.web.client.widgets.sidepanel;

import java.util.ArrayList;
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
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.Shape;
import pl.ismop.web.client.dap.profile.Profile;
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
	private Map<String, Profile> profiles;

	@Inject
	public SidePanelPresenter(DapController dapController) {
		this.dapController = dapController;
		sections = new HashMap<>();
		profiles = new HashMap<>();
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
					eventBus.drawGoogleMap("mainPanel", levees.get(0).getId());
					loadSections(levees.get(0).getId());
				} else {
					view.showNoLeveesLabel(true);
				}
			}
		});
	}
	
	public void onSectionSelectedOnMap(String sectionId) {
		loadSectionStatus(sections.get(sectionId), false);
		view.setSelectedSection(sectionId);
	}
	
	@Override
	public void onSectionChanged(String sectionId) {
		loadSectionStatus(sections.get(sectionId), true);
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
				Window.alert(message);
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

	private void loadSectionStatus(Section section, boolean fireEvent) {
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
			
			if(fireEvent) {
				eventBus.zoomToAndSelectSection(section.getId());
			}
			
			loadProfiles(section.getId());
		} else {
			profiles.clear();
			view.clearProfileValues();
			view.showProfilePanel(false);
			eventBus.zoomToLevee(view.getSelectedLeveeId());
		}
	}

	private void drawProfileLayer(List<Profile> profiles) {
		List<Shape> profileShapes = new ArrayList<>();
		
		for(Profile profile : profiles) {
			if(profile.getShape() != null) {
				profileShapes.add(profile.getShape());
			}
		}
		
		eventBus.drawProfiles(profileShapes);
	}

	private void loadProfiles(String sectionId) {
		profiles.clear();
		view.clearProfileValues();
		view.showProfilePanel(true);
		view.setProfileBusyState(true);
		dapController.getProfiles(sectionId, new ProfilesCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert(message);
			}
			
			@Override
			public void processProfiles(List<Profile> profiles) {
				view.setProfileBusyState(false);
				
				if(profiles.size() > 0) {
					view.showProfileList(true);
					view.addProfileValue(null, view.getPickProfileLabel());
					
					for(Profile profile : profiles) {
						view.addProfileValue(profile.getId(), profile.getId());
						SidePanelPresenter.this.profiles.put(profile.getId(), profile);
						drawProfileLayer(profiles);
					}
				} else {
					view.showNoProfilesLabel(true);
				}
			}
		});
	}
}