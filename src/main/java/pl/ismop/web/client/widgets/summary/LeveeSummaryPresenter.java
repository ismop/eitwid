package pl.ismop.web.client.widgets.summary;

import java.util.Date;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveeCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.summary.ILeveeSummaryView.ILeveeSummaryPresenter;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

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
			public void onError(int code, String message) {
				Window.alert(message);
			}

			@Override
			public void processLevee(Levee levee) {
				LeveeSummaryPresenter.this.levee = levee;
				updateMode();
				eventBus.leveeUpdated(levee);
			}});
	}
	
	public Levee getLevee() {
		return levee;
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
						public void onError(int code, String message) {
							Window.alert("Could not update threat level: " + message);
						}

						@Override
						public void processLevee(Levee levee) {
							LeveeSummaryPresenter.this.levee = levee;
							updateMode();
							updateThreat();
							eventBus.leveeUpdated(levee);
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