package pl.ismop.web.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Singleton;

import java.util.Date;

@Singleton
public class IsmopConverter {
    private final DateTimeFormat dateTimeFormat =
            DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);

    public String format(Date date) {
        return dateTimeFormat.format(date);
    }

    public Date parse(String dateString) {
        return dateTimeFormat.parse(dateString);
    }
}
