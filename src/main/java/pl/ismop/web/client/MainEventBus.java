package pl.ismop.web.client;

import java.util.List;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.experiment.ExperimentPresenter;
import pl.ismop.web.client.widgets.experiments.ExperimentsPresenter;
import pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter;
import pl.ismop.web.client.widgets.newexperiment.ThreatAssessmentPresenter;
import pl.ismop.web.client.widgets.popup.PopupPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;
import pl.ismop.web.client.widgets.sidepanel.SidePanelPresenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.Start;
import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.NoStartPresenter;

@Events(startPresenter = NoStartPresenter.class)
public interface MainEventBus extends EventBus {
	@Start
	@Event(handlers = {RootPresenter.class, SidePanelPresenter.class})
	void start();

	@Event(handlers = GoogleMapsPresenter.class)
	void drawGoogleMap(String elementId, String leveeId);

	@Event
	void leveeUpdated(Levee levee);

	@Event(handlers = ThreatAssessmentPresenter.class)
	void areaSelected(float top, float left, float bottom, float right);

	@Event(handlers = RootPresenter.class)
	void experimentCreated(Experiment experiment);

	@Event(handlers = {ExperimentsPresenter.class, GoogleMapsPresenter.class})
	void showExperiments(List<String> experimentIds);

	@Event(handlers = GoogleMapsPresenter.class)
	void showSensors(boolean show);

	@Event(handlers = GoogleMapsPresenter.class)
	void popupClosed();

	@Event(handlers = PopupPresenter.class)
	void setTitleAndShow(String title, IsWidget widget, boolean resizable);
	
	@Event(handlers = GoogleMapsPresenter.class)
	void zoomToAndSelectSection(String sectionId);

	@Event(handlers = ExperimentPresenter.class)
	void showExperiment();

	@Event(handlers = RootPresenter.class)
	void setSidePanel(IsWidget view);

	@Event(handlers = SidePanelPresenter.class)
	void sectionSelectedOnMap(String sectionId);

	@Event(handlers = GoogleMapsPresenter.class)
	void zoomToLevee(String selectedLeveeId);
}