package pl.ismop.web.client;

import java.util.ArrayList;
import java.util.Collection;
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

	public String merge(Collection<String> chunks, String delimeter) {
		StringBuilder result = new StringBuilder();

		for(String chunk : chunks) {
			result.append(chunk).append(delimeter);
		}

		if(result.length() > 0) {
			result.delete(result.length() - delimeter.length(), result.length());
		}

		return result.toString();
	}

	public String merge(Collection<String> chunks) {
		return merge(chunks, ",");
	}

	public String createSelectionQuery(double top, double left, double bottom, double right) {
		StringBuilder builder = new StringBuilder();
		builder.append("POLYGON ((")
				.append(left).append(" ").append(top).append(", ")
				.append(right).append(" ").append(top).append(", ")
				.append(right).append(" ").append(bottom).append(", ")
				.append(left).append(" ").append(bottom).append(", ")
				.append(left).append(" ").append(top)
				.append("))");

		return builder.toString();
	}

	public String getSectionFillColor(String colourType) {
		switch (colourType) {
			case "A":
				return "#cccccc";
			case "B":
				return "#fbffb9";
			case "C":
				return "#c0c776";
			case "D":
				return "#f3f2f3";
		}
		return "#f9aa4b";
	}
}