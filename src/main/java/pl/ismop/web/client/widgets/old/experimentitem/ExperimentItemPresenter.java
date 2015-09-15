package pl.ismop.web.client.widgets.old.experimentitem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.result.Result;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.old.experimentitem.IExperimentItemView.IExperimentItemPresenter;

@Presenter(view = ExperimentItemView.class, multiple = true)
public class ExperimentItemPresenter extends BasePresenter<IExperimentItemView, MainEventBus> implements IExperimentItemPresenter {
	private Experiment experiment;
	private boolean resultsSet;

	@Inject
	public ExperimentItemPresenter() {
	}

	public void setExperiment(Experiment experiment) {
		if(this.experiment == null) {
			view.getName().setText(experiment.getName());
			view.setDates(experiment.getStartDate(), experiment.getEndDate());
		}
		
		this.experiment = experiment;
		view.setStatus(experiment.getStatus());
		updateResults();
	}
	
	@Override
	public void onShowSection(String sectionId) {
//		eventBus.zoomToAndSelectSection(sectionId);
	}

	private void updateResults() {
		if(experiment.getResults() != null && experiment.getResults().size() > 0) {
			if(!resultsSet) {
				resultsSet = true;
				view.showResultsLabel();
				//<sectionId, list of results sorted by similarity>
				Map<String, List<Result>> sorted = sortResults(experiment.getResults());
				List<String> sectionIds = new ArrayList<>(sorted.keySet());
				Collections.sort(sectionIds, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return Integer.parseInt(o2) - Integer.parseInt(o2);
					}
				});
				
				RESULTS:
				for(int i = 0; ; i++) {
					Map<String, String> threatLevels = new LinkedHashMap<>();
					
					for(String key : sectionIds) {
						if(sorted.get(key).size() > i) {
							threatLevels.put(sorted.get(key).get(i).getSectionId(), sorted.get(key).get(i).getThreatLevel());
						} else {
							break RESULTS;
						}
					}
					
					view.addResultItem(threatLevels);
				}
			}
		} else {
			view.showNoResultsLabel();
		}
	}

	private Map<String, List<Result>> sortResults(List<Result> results) {
		Map<String, List<Result>> sorted = new HashMap<>();
		
		for(Result result : results) {
			if(!sorted.containsKey(result.getSectionId())) {
				sorted.put(result.getSectionId(), new ArrayList<Result>());
			}
			
			sorted.get(result.getSectionId()).add(result);
		}
		
		for(String key : sorted.keySet()) {
			Collections.sort(sorted.get(key), new Comparator<Result>() {
				@Override
				public int compare(Result r1, Result r2) {
					return r2.getSimilarity() > r1.getSimilarity() ? 1 : -1;
				}
			});
		}
		
		return sorted;
	}
}