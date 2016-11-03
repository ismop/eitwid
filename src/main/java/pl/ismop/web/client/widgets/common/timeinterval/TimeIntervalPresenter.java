package pl.ismop.web.client.widgets.common.timeinterval;

import java.util.Date;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.IsmopConverter;
import pl.ismop.web.client.MainEventBus;

@Presenter(view = TimeIntervalView.class, multiple = true)
public class TimeIntervalPresenter extends BasePresenter<ITimeIntervalView, MainEventBus>
        implements ITimeIntervalView.ITimeIntervalPresenter {

    public interface Callback {
        void onSelect(Date from, Date to);
    }

    private Callback callback;

	private IsmopConverter ismopConverter;

	@Inject
    public TimeIntervalPresenter(IsmopConverter ismopConverter) {
		this.ismopConverter = ismopConverter;
    }

    public void show(Date from, Date to, Callback callback) {
        this.callback = callback;
        getView().show(from, to);
    }

    @Override
    public void bind() {
    	super.bind();
    	view.setDateFormatAndLanguage(ismopConverter.getDisplayDateFormat(),
    			ismopConverter.getCurrentLocale());
    }

    @Override
    public void onOk(Date from, Date to) {
        getView().close();
        callback.onSelect(from, to);
        eventBus.removeHandler(this);
    }

    @Override
    public void onCancel() {
        getView().close();
        eventBus.removeHandler(this);
    }
}
