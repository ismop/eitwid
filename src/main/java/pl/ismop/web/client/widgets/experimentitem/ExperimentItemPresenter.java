package pl.ismop.web.client.widgets.experimentitem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ResultsCallback;
import pl.ismop.web.client.dap.result.Result;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.experimentitem.IExperimentItemView.IExperimentItemPresenter;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = ExperimentItemView.class, multiple = true)
public class ExperimentItemPresenter extends BasePresenter<IExperimentItemView, MainEventBus> implements IExperimentItemPresenter {
	private Experiment experiment;
	private DapController dapController;

	@Inject
	public ExperimentItemPresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void setExperiment(Experiment experiment) {
		if(this.experiment == null) {
			this.experiment = experiment;
			view.getName().setText(experiment.getName());
			view.setStartDate(experiment.getStartDate());
			view.setEndDate(experiment.getEndDate());
		}
		
		view.setStatus(experiment.getStatus());
	}

	@Override
	public void onShowResults() {
		dapController.getResults(experiment.getId(), new ResultsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processResults(List<Result> results) {
				showResultsModal();
				DOM.getElementById("results-modal-body").removeAllChildren();
				
				if(results.size() == 0) {
					Element message = DOM.createDiv();
					message.setInnerText(view.getNoResultsMessage());
					DOM.getElementById("results-modal-body").appendChild(message);
				} else {
					Collections.sort(results, new Comparator<Result>() {
						@Override
						public int compare(Result o1, Result o2) {
							int profileCompare = o1.getProfileId().compareTo(o2.getProfileId());
							
							if(profileCompare != 0) {
								return profileCompare;
							} else {
								return Float.compare(o2.getSimilarity(), o1.getSimilarity());
							}
						}
					});
					
					Element tableBody = DOM.createTBody();
					Element table = DOM.createTable();
					table.setAttribute("class", "table table-hover");
					table.appendChild(tableBody);
					
					Element headerRow = DOM.createTR();
					tableBody.appendChild(headerRow);
					
					Element similarityHeader = DOM.createTH();
					similarityHeader.setInnerText(view.getSimilarityLabel());
					headerRow.appendChild(similarityHeader);
					
					Element profileIdHeader = DOM.createTH();
					profileIdHeader.setInnerText(view.getProfileIdLabel());
					headerRow.appendChild(profileIdHeader);
					
					for(Result result : results) {
						Element row = DOM.createTR();
						tableBody.appendChild(row);
						
						Element similarityCell = DOM.createTD();
						similarityCell.setInnerText("" + result.getSimilarity());
						row.appendChild(similarityCell);
						
						Element profileIdCell = DOM.createTD();
						profileIdCell.setInnerText(result.getProfileId());
						row.appendChild(profileIdCell);
					}
					
					Element div = DOM.createDiv();
					div.appendChild(table);
					DOM.getElementById("results-modal-body").appendChild(div);
				}
			}
		});
	}

	private native void showResultsModal() /*-{
		$wnd.jQuery('#results-modal').modal('show');
	}-*/;
	
	private native void hideResultsModal() /*-{
		$wnd.jQuery('#results-modal').modal('hide');
	}-*/;
}