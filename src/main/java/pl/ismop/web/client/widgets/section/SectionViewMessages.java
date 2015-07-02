package pl.ismop.web.client.widgets.section;

import com.google.gwt.i18n.client.Messages;

public interface SectionViewMessages extends Messages {
	String threatLabel();
	String threatLevelUnknown();
	String threatLevelSevere();
	String threatLevelElevated();
	String threatLevelNone();
	String headerLabel(String sectionName);
}
