package pl.ismop.web.client.widgets.common.profile;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface ISideProfileView extends IsWidget {
	interface ISideProfilePresenter {
		void onDeviceSelected(String sensorId, boolean selected);

		void onBack();
	}

	void setScene(String profileName, int width, int height);
	
	void showMeasurement(String measurement);
	
	void removeMeasurement();
	
	String getNoMeasurementLabel();

	void drawProfile(List<List<Double>> profileCoordinates, boolean leftBank, double xShift);

	void removeObjects();

	void drawDevices(Map<String, List<Double>> devicePositions, double xShift);
}