package pl.ismop.web.client.widgets.realtime.main;

import com.google.gwt.i18n.client.Messages;

public interface RealTimePanelMessages extends Messages {
	String changeWeatherTitle();
	
	String weatherStation(String weatherDeviceName);
	
	String chartSectionHeading();
	
	String verticalSliceSectionHeading(String parameterName);
	
	String wateLevelLabel();

	String emptyValue();
	
	String changeVerticalSliceParameterTitle();
}