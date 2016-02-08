package pl.ismop.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Singleton;

@Singleton
public class IsmopConverter {
    private final DateTimeFormat dtoDateTimeFormat =
            DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);
    
    private final DateTimeFormat displayDateTimeFormat =
            DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
    
    private final DateTimeFormat fileSafeNameDateTimeFormat =
            DateTimeFormat.getFormat("yyyy-MM-dd_HH:mm:ss");

    public String formatForDisplay(Date date) {
        return displayDateTimeFormat.format(date);
    }
    
    public String formatForDto(Date date) {
        return dtoDateTimeFormat.format(date);
    }
    
    public List<String> shortMonths() {
    	List<String> result = new ArrayList<>();
    	DateTimeFormat shortMonths = DateTimeFormat.getFormat("MMM");
    	
    	for (int i = 0; i < 12; i++) {
    		result.add(shortMonths.format(new Date(1900, i, 1)));
    	}
    	
    	return result;
    }
    
    public List<String> months() {
    	List<String> result = new ArrayList<>();
    	DateTimeFormat shortMonths = DateTimeFormat.getFormat("MMMM");
    	
    	for (int i = 0; i < 12; i++) {
    		result.add(shortMonths.format(new Date(1900, i, 1)));
    	}
    	
    	return result;
    }

	public List<String> days() {
		List<String> result = new ArrayList<>();
    	DateTimeFormat shortMonths = DateTimeFormat.getFormat("EEEE");
    	
    	for (int i = 1; i <= 7; i++) {
    		//4 added to start with Sunday
    		result.add(shortMonths.format(new Date(1900, 0, i + 4)));
    	}
    	
    	return result;
	}

	public String formatForFileSafeName(Date date) {
		return fileSafeNameDateTimeFormat.format(date);
	}

	public String getDisplayDateFormat() {
		return displayDateTimeFormat.getPattern();
	}
}