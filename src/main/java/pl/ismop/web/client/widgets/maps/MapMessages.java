package pl.ismop.web.client.widgets.maps;

import com.google.gwt.i18n.client.Messages;

public interface MapMessages extends Messages {
	String insufficientPointsError();
	String noFeatureSelected();
	String noMeasurements();
	String sensorTitle(String sensorId);
	String sectionTitle(String profileId);
}