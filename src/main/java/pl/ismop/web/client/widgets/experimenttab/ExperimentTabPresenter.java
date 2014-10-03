package pl.ismop.web.client.widgets.experimenttab;

import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.ExperimentsCallback;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.internal.InternalExperimentController;
import pl.ismop.web.client.internal.InternalExperimentController.UserExperimentsCallback;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class ExperimentTabPresenter extends BaseEventHandler<MainEventBus>{
	private ExperimentTabMessages messages;
	private DapController dapController;
	private InternalExperimentController internalExperimentController;
	private Element span;
	private int numberOfExperiments;
	private List<String> experimentIds;

	@Inject
	public ExperimentTabPresenter(ExperimentTabMessages messages, DapController dapController, InternalExperimentController internalExperimentController) {
		this.messages = messages;
		this.dapController = dapController;
		this.internalExperimentController = internalExperimentController;
	}
	
	public void onStart() {
		internalExperimentController.getExperiments(new UserExperimentsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processUserExperiments(List<String> experimentIds) {
				numberOfExperiments = experimentIds.size();
				createTab(experimentIds);
				
				if(numberOfExperiments == 0) {
					span.setInnerText(messages.experimentTabLabel(numberOfExperiments));
				} else {
					dapController.getExperiments(experimentIds, new ExperimentsCallback() {
						@Override
						public void onError(int code, String message) {
							Window.alert("Error: " + message);
						}
						
						@Override
						public void processExperiments(List<Experiment> experiments) {
							span.setInnerText(messages.experimentTabLabel(numberOfExperiments));
						}
					});
				}
			}

		});
	}
	
	private void createTab(List<String> experimentIds) {
		this.experimentIds = experimentIds;
		
		Element li = DOM.createElement("li");
		Element a = DOM.createAnchor();
		a.setAttribute("href", "#");
		DOM.sinkEvents(a, Event.ONCLICK);
		DOM.setEventListener(a, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				eventBus.showExperiments(ExperimentTabPresenter.this.experimentIds);
				span.getStyle().clearFontWeight();
			}
		});
		
		Element icon = DOM.createElement("i");
		icon.setAttribute("class", "fa fa-bug fa-fw");
		a.appendChild(icon);
		
		span = DOM.createSpan();
		a.appendChild(span);
		li.appendChild(a);
		RootPanel.get("side-menu").getElement().appendChild(li);
	}
	
	public void onExperimentCreated(Experiment experiment) {
		numberOfExperiments++;
		experimentIds.add(experiment.getId());
		span.setInnerText(messages.experimentTabLabel(numberOfExperiments));
		span.getStyle().setFontWeight(FontWeight.BOLD);
	}
}