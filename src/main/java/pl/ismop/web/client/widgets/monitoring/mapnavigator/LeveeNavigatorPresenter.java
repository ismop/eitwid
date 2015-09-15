package pl.ismop.web.client.widgets.monitoring.mapnavigator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.common.map.MapPresenter;
import pl.ismop.web.client.widgets.monitoring.mapnavigator.ILeveeNavigatorView.ILeveeNavigatorPresenter;

@Presenter(view = LeveeNavigatorView.class, multiple = true)
public class LeveeNavigatorPresenter extends BasePresenter<ILeveeNavigatorView, MainEventBus> implements ILeveeNavigatorPresenter {
	private MapPresenter mapPresenter;
	private Levee displayedLevee;
	private DapController dapController;

	@Inject
	public LeveeNavigatorPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	@Override
	public void bind() {
		eventBus.leveeNavigatorReady();
	}
	
	public void onLeveeSelected(Levee levee) {
		if(displayedLevee == null || !displayedLevee.getId().equals(levee.getId())) {
			displayedLevee = levee;
			
			if(mapPresenter == null) {
				mapPresenter = eventBus.addHandler(MapPresenter.class);
				mapPresenter.addHoverListeners();
			}
			
			mapPresenter.reset();
			view.showMap(false);
			view.showProgress(true);
			dapController.getSections(displayedLevee.getId(), new SectionsCallback() {
				@Override
				public void onError(ErrorDetails errorDetails) {
					view.showProgress(false);
					eventBus.showError(errorDetails);
				}
				
				@Override
				public void processSections(final List<Section> sections) {
					List<String> sectionIds = collectSectionIds(sections);
					dapController.getProfiles(sectionIds, new ProfilesCallback() {
						
						@Override
						public void onError(ErrorDetails errorDetails) {
							view.showProgress(false);
							eventBus.showError(errorDetails);
						}
						
						@Override
						public void processProfiles(List<Profile> profiles) {
							view.showProgress(false);
							view.showMap(true);
							view.setMap(mapPresenter.getView());
							
							for(Section section : sections) {
								mapPresenter.addSection(section);
							}
							
							for(Profile profile : profiles) {
								mapPresenter.addProfile(profile);
							}
						}
					});
				}
			});
		}
	}
	
	private List<String> collectSectionIds(List<Section> sections) {
		List<String> result = new ArrayList<>();
		
		for(Section section : sections) {
			result.add(section.getId());
		}
		
		return result;
	}
}