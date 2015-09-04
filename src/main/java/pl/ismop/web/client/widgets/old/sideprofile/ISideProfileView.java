package pl.ismop.web.client.widgets.old.sideprofile;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISideProfileView extends IsWidget {
	interface ISideProfilePresenter {
		void onSensorSelected(String sensorId, boolean selected);
	}

	void setScene(String profileName, List<String> sensorIds);
	void clearSensors();
	void showMeasurement(String measurement);
	void removeMeasurement();
	String getNoMeasurementLabel();
}