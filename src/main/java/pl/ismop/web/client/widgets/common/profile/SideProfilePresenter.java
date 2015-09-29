package pl.ismop.web.client.widgets.common.profile;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayUtils;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.widgets.common.profile.ISideProfileView.ISideProfilePresenter;

@Presenter(view = SideProfileView.class, multiple = true)
public class SideProfilePresenter extends BasePresenter<ISideProfileView, MainEventBus> implements ISideProfilePresenter {
	private static final double PROFILE_HEIGHT = 4.5;
	
	private static final double PROFILE_TOP_WIDTH = 4;
	
	private String selectedSensorId;
	
	private int width, height;
	
	public void setProfileAndDevices(Profile profile, List<Device> devices) {
		view.setScene(profile.getId(), width, height);
		
		List<List<Double>> fullProfile = createFullProfile(projectCoordinates(profile.getShape().getCoordinates()));
		boolean leftBank = true;

		//TODO: come up with a better way of telling where the water is
		if(profile.getShape().getCoordinates().get(0).get(0) > 19.676851838778) {
			leftBank = false;
		}
		
		double xShift = -fullProfile.get(1).get(0) / 2;
		view.drawProfile(fullProfile, leftBank, xShift);
		
		Map<String, List<Double>> devicePositions = collectDevicePositions(getReferencePoint(profile.getShape().getCoordinates()), devices);
		view.drawDevices(devicePositions, xShift);
	}

	public void onDeviceSelected(final String sensorId, boolean selected) {
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

	@Override
	public void onBack() {
		eventBus.backFromSideProfile();
	}

	public void clear() {
		view.removeObjects();
	}

	private List<Double> getReferencePoint(List<List<Double>> coordinates) {
		Collections.sort(coordinates, new Comparator<List<Double>>() {
			@Override
			public int compare(List<Double> o1, List<Double> o2) {
				return o1.get(0).compareTo(o2.get(0));
			}
		});
		
		return coordinates.get(0);
	}

	private Map<String, List<Double>> collectDevicePositions(List<Double> referencePoint, List<Device> devices) {
		Map<String, List<Double>> result = new HashMap<>();
		List<List<Double>> referenceSource = new ArrayList<>();
		referenceSource.add(referencePoint);
		
		List<Double> projectedReference = projectCoordinates(referenceSource).get(0);
		
		for(Device device : devices) {
			List<List<Double>> coordinates = new ArrayList<List<Double>>();
			coordinates.add(device.getPlacement().getCoordinates());
			
			List<List<Double>> projectedValues = projectCoordinates(coordinates);
			double distance = sqrt(
				pow(projectedValues.get(0).get(0) - projectedReference.get(0), 2) +
				pow(projectedValues.get(0).get(1) - projectedReference.get(1), 2)
			);
			
			//for now let's position the devices half meter above ground
			result.put(device.getId(), Arrays.asList(distance, 0.5));
		}
		
		return result;
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

	private List<List<Double>> projectCoordinates(List<List<Double>> coordinates) {
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
}