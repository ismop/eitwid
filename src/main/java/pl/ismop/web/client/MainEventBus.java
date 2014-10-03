package pl.ismop.web.client;

import java.util.List;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.widgets.experiments.ExperimentsPresenter;
import pl.ismop.web.client.widgets.experimenttab.ExperimentTabPresenter;
import pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter;
import pl.ismop.web.client.widgets.newexperiment.ExperimentPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.Start;
import com.mvp4g.client.event.EventBus;
import com.mvp4g.client.presenter.NoStartPresenter;

@Events(startPresenter = NoStartPresenter.class)
public interface MainEventBus extends EventBus {
	@Start
	@Event(handlers = {RootPresenter.class, ExperimentTabPresenter.class})
	void start();

	@Event(handlers = GoogleMapsPresenter.class)
	void drawGoogleMap(String elementId, String detailsElementId);

	@Event(handlers = GoogleMapsPresenter.class)
	void leveeUpdated(Levee levee);

	@Event(handlers = ExperimentPresenter.class)
	void areaSelected(float top, float left, float bottom, float right);

	@Event(handlers = ExperimentTabPresenter.class)
	void experimentCreated(Experiment experiment);

	@Event(handlers = ExperimentsPresenter.class)
	void showExperiments(List<String> experimentIds);
}