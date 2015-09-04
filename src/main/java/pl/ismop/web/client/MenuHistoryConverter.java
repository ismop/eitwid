package pl.ismop.web.client;

import com.mvp4g.client.annotation.History;
import com.mvp4g.client.annotation.History.HistoryConverterType;
import com.mvp4g.client.history.HistoryConverter;

@History(type = HistoryConverterType.NONE)
public class MenuHistoryConverter implements HistoryConverter<MainEventBus> {
	@Override
	public void convertFromToken(String historyName, String param, MainEventBus eventBus) {
		eventBus.dispatch(historyName);
	}

	@Override
	public boolean isCrawlable() {
		return false;
	}
}