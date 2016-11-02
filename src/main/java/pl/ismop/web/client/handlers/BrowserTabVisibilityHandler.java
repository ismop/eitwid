package pl.ismop.web.client.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

import elemental2.Event;
import elemental2.Global;
import pl.ismop.web.client.MainEventBus;

@EventHandler
public class BrowserTabVisibilityHandler extends BaseEventHandler<MainEventBus> {

	private final static Logger log = LoggerFactory.getLogger(BrowserTabVisibilityHandler.class);

	public BrowserTabVisibilityHandler() {
		Global.document.addEventListener("visibilitychange", this::handleVisibilityChange, false);
	}

	private void handleVisibilityChange(Event event) {
		log.info("Detected app visibility change - hidden: " + Global.document.hidden);
		getEventBus().appVisibilityChange(Global.document.hidden);
	}
}
