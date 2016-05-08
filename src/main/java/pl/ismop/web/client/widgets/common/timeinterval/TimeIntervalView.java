package pl.ismop.web.client.widgets.common.timeinterval;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.constants.DateTimePickerLanguage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class TimeIntervalView extends Composite implements ITimeIntervalView,
        ReverseViewInterface<ITimeIntervalView.ITimeIntervalPresenter> {
    private static TimeIntervalUiBinder uiBinder = GWT.create(TimeIntervalUiBinder.class);
    
    interface TimeIntervalUiBinder extends UiBinder<Widget, TimeIntervalView> {}

    private ITimeIntervalPresenter presenter;

    @UiField
    Modal modal;

    @UiField
    DateTimePicker fromDate;

    @UiField
    DateTimePicker toDate;

    public TimeIntervalView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void show(Date from, Date to) {
        fromDate.setValue(from);
        toDate.setValue(to);
        modal.show();
    }

    @Override
    public void close() {
        modal.hide();
    }

    @UiHandler("ok")
    void ok(ClickEvent e) {
        getPresenter().onOk(fromDate.getValue(), toDate.getValue());
    }

    @UiHandler("cancel")
    void cancel(ClickEvent e) {
        getPresenter().onCancel();
    }

    @Override
    public void setPresenter(ITimeIntervalPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public ITimeIntervalPresenter getPresenter() {
        return presenter;
    }

	@Override
	public void setDateFormatAndLanguage(String dateFormat, String language) {
		fromDate.setGWTFormat(dateFormat);
		fromDate.setLanguage(DateTimePickerLanguage.valueOf(language.toUpperCase()));
		toDate.setGWTFormat(dateFormat);
		toDate.setLanguage(DateTimePickerLanguage.valueOf(language.toUpperCase()));
	}
}