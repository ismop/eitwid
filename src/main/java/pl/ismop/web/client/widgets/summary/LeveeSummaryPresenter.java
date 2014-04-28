package pl.ismop.web.client.widgets.summary;

import java.util.Date;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveeModeChangedCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.summary.ILeveeSummaryView.ILeveeSummaryPresenter;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

@Presenter(view = LeveeSummaryView.class, multiple = true)
public class LeveeSummaryPresenter extends BasePresenter<ILeveeSummaryView, MainEventBus> implements ILeveeSummaryPresenter {
	private Levee levee;
	private DapController dapController;
	
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
		dapController.changeLeveeMode(levee.getId(), mode, new LeveeModeChangedCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert(message);
			}

			@Override
			public void processLevee(Levee levee) {
				LeveeSummaryPresenter.this.levee = levee;
				view.setMode(levee.getEmergencyLevel());
				view.addModePanelStyle(getPanelStyle(levee.getEmergencyLevel()));
			}});
	}

	private void showLeveeDetails() {
		view.setHeader(levee.getName());
		view.setMode(levee.getEmergencyLevel());
		view.setThreat(levee.getThreatLevel());
		
		String timestamp = levee.getThreatLevelLastUpdate();
		DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
		Date date = format.parse(timestamp);
		String formattedDate = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(date);
		view.setThreatLastUpdated(formattedDate);
		view.addModePanelStyle(getPanelStyle(levee.getEmergencyLevel()));
		view.addThreatPanelStyle(getPanelStyle(levee.getThreatLevel()));
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