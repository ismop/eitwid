package pl.ismop.web.client.widgets.root;

import java.util.List;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class RootPresenter extends BaseEventHandler<MainEventBus> {
	private static final String DETAILS_CONTAINER_ID = "detailsContainer";
	private static final String SUMMARY_CONTAINER_ID = "leveesContainer";
	private static final String MAP_CONTAINER_ID = "mapContainer";
	
	private DapController dapController;

	@Inject
	public RootPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void onStart() {
		if(RootPanel.get(SUMMARY_CONTAINER_ID) != null) {
			addSpinner();
			dapController.getLevees(new LeveesCallback() {
				@Override
				public void onError(int code, String message) {
					removeSpinner();
					Window.alert("Error: " + message);
				}
				
				@Override
				public void processLevees(List<Levee> levees) {
					removeSpinner();
					
					for(Levee levee : levees) {
						LeveeSummaryPresenter presenter = eventBus.addHandler(LeveeSummaryPresenter.class);
						presenter.setLevee(levee);
						RootPanel.get(SUMMARY_CONTAINER_ID).add(presenter.getView());
					}
				}
			});
		} else if(RootPanel.get(MAP_CONTAINER_ID) != null) {
			eventBus.drawGoogleMap(MAP_CONTAINER_ID, DETAILS_CONTAINER_ID);
		}
	}

	private void addSpinner() {
		RootPanel.get(SUMMARY_CONTAINER_ID).add(createSpinner());
		RootPanel.get(SUMMARY_CONTAINER_ID).getElement().getStyle().setTextAlign(TextAlign.CENTER);
	}
	
	private void removeSpinner() {
		RootPanel.get(SUMMARY_CONTAINER_ID).clear();
		RootPanel.get(SUMMARY_CONTAINER_ID).getElement().getStyle().clearTextAlign();
	}

	private Widget createSpinner() {
		HTMLPanel panel = new HTMLPanel("<i class='fa fa-spinner fa-2x fa-spin'></i>");
		
		return panel;
	}
}