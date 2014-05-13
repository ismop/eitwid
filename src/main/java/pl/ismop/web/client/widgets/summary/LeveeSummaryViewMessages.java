package pl.ismop.web.client.widgets.summary;

import com.google.gwt.i18n.client.Messages;

public interface LeveeSummaryViewMessages extends Messages {
	String modeLabel();
	String threatLabel();
	String threatLastUpdated(String threatLastUpdated);
	String modeChangeLabel();
	String stantbyModeLabel();
	String alertModeLabel();
	String threatModeLabel();
	String threatLevelUnknown();
	String threatLevelSevere();
	String threatLevelElevated();
	String threatLevelNone();
	String modeUnknown(String mode);
}