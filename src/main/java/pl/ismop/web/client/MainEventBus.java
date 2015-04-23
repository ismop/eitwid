package pl.ismop.web.client;

import java.util.List;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.experiments.ExperimentsPresenter;
import pl.ismop.web.client.widgets.levees.LeveesPresenter;
import pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter;
import pl.ismop.web.client.widgets.newexperiment.ExperimentPresenter;
import pl.ismop.web.client.widgets.popup.PopupPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;

import com.google.gwt.user.client.ui.IsWidget;
import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.Start;
import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.NoStartPresenter;

@Events(startPresenter = NoStartPresenter.class)
public interface MainEventBus extends EventBus {
	@Start
	@Event(handlers = RootPresenter.class)
	void start();

	@Event(handlers = GoogleMapsPresenter.class)
	void drawGoogleMap(String elementId);

	@Event
	void leveeUpdated(Levee levee);

	@Event(handlers = ExperimentPresenter.class)
	void areaSelected(float top, float left, float bottom, float right);

	@Event(handlers = RootPresenter.class)
	void experimentCreated(Experiment experiment);

	@Event(handlers = {ExperimentsPresenter.class, GoogleMapsPresenter.class})
	void showExperiments(List<String> experimentIds);

	@Event(handlers = LeveesPresenter.class)
	void showLeveeList();

	@Event(handlers = GoogleMapsPresenter.class)
	void showSensors(boolean show);

	@Event(handlers = {GoogleMapsPresenter.class, LeveesPresenter.class})
	void popupClosed();

	@Event(handlers = PopupPresenter.class)
	void setTitleAndShow(String title, IsWidget widget, boolean resizable);
	
	@Event(handlers = GoogleMapsPresenter.class)
	void zoomToSection(String sectionId);
}