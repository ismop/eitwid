package pl.ismop.web.client.widgets.common.profile;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISideProfileView extends IsWidget {
	interface ISideProfilePresenter {
		void onSensorSelected(String sensorId, boolean selected);
	}

	void setScene(String profileName, int width, int height);
	
	void clearSensors();
	
	void showMeasurement(String measurement);
	
	void removeMeasurement();
	
	String getNoMeasurementLabel();

	void drawProfile(List<List<Double>> profileCoordinates);
}