package pl.ismop.web.client;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter;
import pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;

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
	void drawGoogleMap(String elementId, String detailsElementId);

	@Event(handlers = {GoogleMapsPresenter.class, OpenLayersMapsPresenter.class})
	void leveeUpdated(Levee levee);

	@Event(handlers = OpenLayersMapsPresenter.class)
	void drawOpenLayersMap(String mapContainerId, String detailsContainerId);
}