package pl.ismop.web.client;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.annotation.InitHistory;
import com.mvp4g.client.event.EventBusWithLookup;

import pl.ismop.web.client.error.ErrorDetails;
import pl.ismop.web.client.widgets.error.ErrorPresenter;
import pl.ismop.web.client.widgets.root.RootPresenter;

@Events(startPresenter = RootPresenter.class, historyOnStart = true)
public interface MainEventBus extends EventBusWithLookup {
	@InitHistory
	@Event(handlers = RootPresenter.class, historyConverter = MenuHistoryConverter.class)
	void monitoringPanel();
	
	@Event(handlers = RootPresenter.class, historyConverter = MenuHistoryConverter.class)
	void analysisPanel();

	@Event(handlers = ErrorPresenter.class)
	void showError(ErrorDetails errorDetails);
}