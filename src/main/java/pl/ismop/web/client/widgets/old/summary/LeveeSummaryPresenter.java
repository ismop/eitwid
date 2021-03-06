package pl.ismop.web.client.widgets.old.summary;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveeCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.old.summary.ILeveeSummaryView.ILeveeSummaryPresenter;

@Presenter(view = LeveeSummaryView.class, multiple = true)
public class LeveeSummaryPresenter extends BasePresenter<ILeveeSummaryView, MainEventBus> implements ILeveeSummaryPresenter {
	private Levee levee;
	private DapController dapController;
	private Timer threatTimer;
	
	@Inject
	public LeveeSummaryPresenter(DapController dapController) {
		this.dapController = dapController;
	}

	public void setLevee(Levee levee) {
		this.levee = levee;
		showLeveeDetails();
	}
	
	@Override
	public void changeMode(String mode) {
		dapController.changeLeveeMode(levee.getId(), mode, new LeveeCallback() {
			@Override
			public void onError(ErrorDetails errorDetails) {
				Window.alert("Error: " + errorDetails.getMessage());
			}

			@Override
			public void processLevee(Levee levee) {
				LeveeSummaryPresenter.this.levee = levee;
				updateMode();
//				eventBus.leveeUpdated(levee);
			}});
	}
	
	public Levee getLevee() {
		return levee;
	}
	
	public void stopUpdate() {
		if(threatTimer != null) {
			threatTimer.cancel();
			threatTimer = null;
		}
	}

	private void showLeveeDetails() {
		view.setHeader(levee.getName());
		updateMode();
		updateThreat();
	}

	private void updateThreat() {
		view.setThreat(levee.getThreatLevel());
		
		String timestamp = levee.getThreatLevelLastUpdate();
		DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
		Date date = format.parse(timestamp);
		String formattedDate = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(date);
		view.setThreatLastUpdated(formattedDate);
		view.addThreatPanelStyle(getPanelStyle(levee.getThreatLevel()));
		
		if(threatTimer == null) {
			threatTimer = new Timer() {
				@Override
				public void run() {
					threatTimer = null;
					dapController.getLevee(levee.getId(), new LeveeCallback() {
						@Override
						public void onError(ErrorDetails errorDetails) {
							Window.alert("Could not update threat level: " + errorDetails.getMessage());
						}

						@Override
						public void processLevee(Levee levee) {
							LeveeSummaryPresenter.this.levee = levee;
							updateMode();
							updateThreat();
//							eventBus.leveeUpdated(levee);
						}});
				}
			};
			threatTimer.schedule(10000);
		}
	}

	private void updateMode() {
		view.setMode(levee.getEmergencyLevel());
		view.addModePanelStyle(getPanelStyle(levee.getEmergencyLevel()));
	}

	private String getPanelStyle(String level) {
		switch(level) {
			case "none":
				return "text-success";
			case "heightened":
				return "text-warning";
			case "severe":
				return "text-danger";
			default:
				return "text-default";
		}
	}
}