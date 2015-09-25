package pl.ismop.web.client.widgets.common.profile;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayUtils;
import com.google.gwt.user.client.Window;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.widgets.old.section.ISectionView.ISectionPresenter;

@Presenter(view = SideProfileView.class, multiple = true)
public class SideProfilePresenter extends BasePresenter<ISideProfileView, MainEventBus> implements ISectionPresenter {
	private static final double PROFILE_HEIGHT = 4.5;
	
	private static final double PROFILE_TOP_WIDTH = 4;
	
	private String selectedSensorId;
	
	private int width, height;
	
	public void setProfileAndDevices(Profile profile, List<Device> devices) {
//		List<String> sensorIds = new ArrayList<>();
//		
//		for(Sensor sensor : devices) {
//			sensorIds.add(sensor.getId());
//		}
//		
//		view.clearSensors();
		view.setScene(profile.getId(), width, height);
		
		List<List<Double>> fullProfile = createFullProfile(projectProfileCoordinates(profile.getShape().getCoordinates()));
		view.drawProfile(fullProfile);
	}

	private List<List<Double>> createFullProfile(List<List<Double>> profile) {
		List<List<Double>> result = new ArrayList<>();
		double length = sqrt(
			pow(profile.get(0).get(0) - profile.get(1).get(0), 2) +
			pow(profile.get(0).get(1) - profile.get(1).get(1), 2)
		);
		result.add(asList(new Double[] {0.0, 0.0}));
		result.add(asList(new Double[] {length, 0.0}));
		result.add(asList(new Double[] {(length - PROFILE_TOP_WIDTH) / 2, PROFILE_HEIGHT}));
		result.add(asList(new Double[] {(length - PROFILE_TOP_WIDTH) / 2 + PROFILE_TOP_WIDTH, PROFILE_HEIGHT}));
		
		return result;
	}

	private List<List<Double>> projectProfileCoordinates(List<List<Double>> coordinates) {
		JsArray<JsArrayNumber> sourceCoordinates = (JsArray<JsArrayNumber>) JsArray.createArray();
		
		for(List<Double> cordinatePair : coordinates) {
			sourceCoordinates.push(JsArrayUtils.readOnlyJsArray(new double[] {cordinatePair.get(0), cordinatePair.get(1)}));
		}
		
		JsArray<JsArrayNumber> projected = convertCoordinates(sourceCoordinates);
		List<List<Double>> result = new ArrayList<>();
		
		for(int i = 0; i < projected.length(); i++) {
			List<Double> coordinatePair = new ArrayList<>();
			coordinatePair.add(projected.get(i).get(0));
			coordinatePair.add(projected.get(i).get(1));
			result.add(coordinatePair);
		}
		
		return result;
	}

	private native JsArray<JsArrayNumber> convertCoordinates(JsArray<JsArrayNumber> sourceCoordinates) /*-{
		var output = [];
		
		sourceCoordinates.forEach(function (elem) {
			var pcs2000 = '+proj=tmerc +lat_0=0 +lon_0=21 +k=0.999923 +x_0=7500000 +y_0=0 +ellps=GRS80 +units=m +no_defs ';
			var coords = $wnd.proj4(pcs2000, [elem[0], elem[1]]);
			output.push(coords)
		});
		
		return output;
	}-*/;

	public void onSensorSelected(final String sensorId, boolean selected) {
		if(selected) {
			selectedSensorId = sensorId;
//			dapController.getSensor(sensorId, new SensorCallback() {
//				@Override
//				public void onError(ErrorDetails errorDetails) {
//					Window.alert("Error: " + errorDetails.getMessage());
//				}
//				
//				@Override
//				public void processSensor(final Sensor sensor) {
//					dapController.getMeasurements(sensorId, new MeasurementsCallback() {
//						@Override
//						public void onError(ErrorDetails errorDetails) {
//							Window.alert("Error: " + errorDetails.getMessage());
//						}
//						
//						@Override
//						public void processMeasurements(List<Measurement> measurements) {
//							if(selectedSensorId != null && selectedSensorId.equals(sensorId)) {
//								if(measurements != null && measurements.size() > 0) {
//									sort(measurements, new Comparator<Measurement>() {
//										@Override
//										public int compare(Measurement m1, Measurement m2) {
//											Date d1 = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).parse(m1.getTimestamp());
//											Date d2 = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).parse(m2.getTimestamp());
//											
//											if(d1.after(d2)) {
//												return -1;
//											} else {
//												return 1;
//											}
//										}
//									});
//									double value = Math.round(measurements.get(0).getValue() * 100);
//									view.showMeasurement("Sensor " + sensor.getCustomId() + ": " + value / 100 + " " + sensor.getUnit());
//								} else {
//									view.showMeasurement(view.getNoMeasurementLabel());
//								}
//							}
//						}
//					});
//				}
//			});
		} else {
			view.removeMeasurement();
			selectedSensorId = null;
		}
	}

	public void setWidthAndHeight(int width, int height) {
		this.width = width;
		this.height = height;
	}
}