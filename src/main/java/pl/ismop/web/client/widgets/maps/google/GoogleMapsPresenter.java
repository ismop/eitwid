package pl.ismop.web.client.widgets.maps.google;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.ProfilesCallback;
import pl.ismop.web.client.dap.DapController.SensorCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.widgets.maps.MapMessages;
import pl.ismop.web.client.widgets.newexperiment.ExperimentPresenter;
import pl.ismop.web.client.widgets.profile.ProfilePresenter;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class GoogleMapsPresenter extends BaseEventHandler<MainEventBus> {
	private static final Logger log = LoggerFactory.getLogger(GoogleMapsPresenter.class);
	
	private DapController dapController;
	private String elementId;
	private MapMessages messages;
	private Map<String, Profile> profiles;
	private String detailsElementId;
	private ProfilePresenter selectedProfile;
	private JavaScriptObject map;
	private Map<String, String> profileColors;
	private Sensor selectedSensor;
	private JavaScriptObject currentGraph;
	private Timer sensorTimer;
	private ListBox days;

	@Inject
	public GoogleMapsPresenter(DapController dapController, MapMessages messages) {
		this.dapController = dapController;
		this.messages = messages;
		profiles = new HashMap<>();
		profileColors = new HashMap<>();
		profileColors.put("none", "#D9EDF7");
		profileColors.put("heightened", "#FAEBCC");
		profileColors.put("severe", "#EBCCD1");
	}
	
	public void onDrawGoogleMap(String mapElementId, String detailsElementId) {
		this.elementId = mapElementId;
		this.detailsElementId = detailsElementId;
		showProgressIndicator(true);
		setNoFeatureSelectedLabel();
		dapController.getProfiles(new ProfilesCallback() {
			@Override
			public void onError(int code, String message) {
				showProgressIndicator(false);
				Window.alert("Error: " + message);
			}

			@Override
			public void processProfiles(List<Profile> profiles) {
				showProgressIndicator(false);
				List<List<Double>> allPoints = new ArrayList<List<Double>>();

				for(Profile profile : profiles) {
					GoogleMapsPresenter.this.profiles.put(profile.getId(), profile);
					allPoints.addAll(profile.getShape().getCoordinates());
				}
				
				if(allPoints.size() > 1) {
					Object bounds = createLatLngBounds();
					
					for(List<Double> point : allPoints) {
						extend(bounds, point.get(1), point.get(0));
					}
					
					Element element = DOM.getElementById(GoogleMapsPresenter.this.elementId);
					
					if(element != null) {
						map = showMap(bounds);
					}
				} else {
					showInsufficientPointsError();
				}
			}
			
		});
	}
	
	public void onProfileUpdated(Profile profile) {
		if(profiles.get(profile.getId()) != null) {
			profiles.put(profile.getId(), profile);
			updateProfileOnMap(profile.getId());
		}
	}
	
	public void onShowExperiments(List<String> experimentIds) {
		if(selectedProfile != null) {
			selectedProfile.stopUpdate();
		}
		
		if(selectedSensor != null) {
			if(sensorTimer != null) {
				sensorTimer.cancel();
				sensorTimer = null;
			}
		}
	}

	private void setNoFeatureSelectedLabel() {
		Element element = DOM.getElementById(detailsElementId);
		
		if(element != null) {
			element.setInnerText(messages.noFeatureSelected());
		}
	}
	
	private void setNoMeasurementsLabel() {
		Element element = DOM.getElementById(detailsElementId);
		
		if(element != null) {
			element.setInnerText(messages.noMeasurements());
		}
	}

	private void showInsufficientPointsError() {
		Element element = DOM.getElementById(elementId);
		
		if(element != null) {
			element.setInnerHTML("<div class='alert alert-danger'>" + messages.insufficientPointsError() + "</div>");
		}
	}

	private void showProgressIndicator(boolean show) {
		if(show) {
			Element element = DOM.getElementById(elementId);
			
			if(element != null) {
				element.getStyle().setTextAlign(TextAlign.CENTER);
				element.setInnerHTML("<i class='fa fa-spinner fa-spin fa-2x'></i>");
			}
		} else {
			Element element = DOM.getElementById(elementId);
			
			if(element != null) {
				element.setInnerHTML("");
			}
		}
	}
	
	private  void onFeatureClicked(String featureId) {
		log.info("Feature with id {} clicked", featureId);
		
		if(RootPanel.get(detailsElementId) != null) {
			if(getProfileId(featureId) != null) {
				Profile profile = profiles.get(getProfileId(featureId));
				showProfileDetails(profile);
			} else if(getSensorId(featureId) != null) {
				showSensorDetails(getSensorId(featureId));
			}
		}
	}

	private void showSensorDetails(final String sensorId) {
		if(sensorTimer != null) {
			sensorTimer.cancel();
			sensorTimer = null;
		}
		
		dapController.getSensor(sensorId, new SensorCallback() {
			@Override
			public void onError(int code, String message) {
				setNoFeatureSelectedLabel();
				selectSensor(sensorId, false);
				Window.alert("Error: " + message);
			}

			@Override
			public void processSensor(final Sensor sensor) {
				if(selectedProfile != null) {
					String previousProfileId = selectedProfile.getProfile().getId();
					eventBus.removeHandler(selectedProfile);
					selectedProfile.stopUpdate();
					selectedProfile = null;
					selectProfile(previousProfileId, false);
				}
				
				dapController.getMeasurements(sensor.getId(), new MeasurementsCallback() {
					@Override
					public void onError(int code, String message) {
						setNoFeatureSelectedLabel();
						Window.alert("Error: " + message);
					}

					@Override
					public void processMeasurements(List<Measurement> measurements) {
						if(measurements.size() == 0) {
							setNoMeasurementsLabel();
						} else {
							if(selectedSensor != null) {
								selectSensor(selectedSensor.getId(), false);
							}
							
							selectedSensor = sensor;
							selectSensor(selectedSensor.getId(), true);
							
							JavaScriptObject values = JavaScriptObject.createArray();
							double min = Double.MAX_VALUE;
							double max = Double.MIN_VALUE;
							
							for(Measurement measurement : measurements) {
								push(measurement.getValue(), measurement.getTimestamp(), values);
								
								if(measurement.getValue() < min) {
									min = measurement.getValue();
								}
								
								if(measurement.getValue() > max) {
									max = measurement.getValue();
								}
							}
							
							double diff = max- min;
							min = min - 0.1 * diff;
							max = max + 0.1 * diff;
							
							Element element = DOM.getElementById(detailsElementId);
							element.setInnerHTML("");
							
							Element chart = DOM.createDiv();
							chart.setId("measurements");
							chart.getStyle().setHeight(250, Unit.PX);
							RootPanel.get(detailsElementId).getElement().appendChild(chart);
							
							
							String unit = sensor.getUnit() == null ? "unknown" : sensor.getUnit();
							String unitLabel = sensor.getUnitLabel() == null ? "unknown" : sensor.getUnitLabel();
							currentGraph = showDygraphChart(getDygraphValues(measurements, unitLabel), unitLabel + ", " + unit,
									unitLabel + " (" + sensor.getCustomId() + ")");
							sensorTimer = new Timer() {
								@Override
								public void run() {
									updateSensorDetails(sensor.getId());
								}
							};
							sensorTimer.schedule(10000);
						}
					}});
			}
		});
	}
	
	private void updateSensorDetails(final String sensorId) {
		dapController.getMeasurements(sensorId, new MeasurementsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}

			@Override
			public void processMeasurements(List<Measurement> measurements) {
				String data = getDygraphValues(measurements, selectedSensor.getUnitLabel());
				updateDygraphData(data);
				sensorTimer = new Timer() {
					@Override
					public void run() {
						updateSensorDetails(sensorId);
					}
				};
				sensorTimer.schedule(10000);
			}});
	}
	
	private String getDygraphValues(List<Measurement> measurements, String yLabel) {
		StringBuilder builder = new StringBuilder();
		builder.append("aa|").append(yLabel).append("\n");
		
		for(Measurement measurement : measurements) {
			DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
			Date date = format.parse(measurement.getTimestamp());
			date = new Date(date.getTime() - 7200000);
			builder.append(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT).format(date))
					.append("|")
					.append(measurement.getValue())
					.append("\n");
		}
		
		return builder.toString();
	}

	private void showProfileDetails(Profile profile) {
		if(sensorTimer != null) {
			sensorTimer.cancel();
			sensorTimer = null;
		}
		
		if(selectedSensor != null) {
			selectSensor(selectedSensor.getId(), false);
		}
		
		selectedSensor = null;
		currentGraph = null;
		
		String previousProfileId = null;
		
		if(selectedProfile != null) {
			previousProfileId = selectedProfile.getProfile().getId();
			eventBus.removeHandler(selectedProfile);
			selectedProfile.stopUpdate();
			selectedProfile = null;
		}
		
		Element element = DOM.getElementById(detailsElementId);
		element.setInnerHTML("");
		
		ProfilePresenter presenter = eventBus.addHandler(ProfilePresenter.class);
		selectedProfile = presenter;
		presenter.setProfile(profile);
		RootPanel.get(detailsElementId).add(presenter.getView());
		
		if(previousProfileId != null) {
			selectProfile(previousProfileId, false);
		}
		
		selectProfile(profile.getId(), true);
	}
	
	private String getFeatureColor(String featureId) {
		String profileId = getProfileId(featureId);
		
		if(profileId != null) {
			for(Profile profile : profiles.values()) {
				if(profile.getId().equals(profileId)) {
					return profileColors.get(profile.getThreatLevel());
				}
			}
		}
		
		return "white";
	}
	
	private String getFeatureStrokeColor(String featureId) {
		switch(getFeatureType(featureId)) {
			case "levee":
				return "black";
			case "profile":
				return "green";
		}
		
		return "white";
	}
	
	private int getFeatureStrokeWeight(String featureId) {
		switch(getFeatureType(featureId)) {
			case "levee":
				return 1;
			case "profile":
				return 1;
		}
		
		return 0;
	}
	
	private void onAreaSelected(float top, float left, float bottom, float right) {
		eventBus.addHandler(ExperimentPresenter.class);
		eventBus.areaSelected(top, left, bottom, right);
	}
	
	private native void updateProfileOnMap(String leveeId) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
		var foundFeature = null;
		geoJsonMap.data.forEach(function(feature) {
			if(feature.getProperty('type') == 'profile' && leveeId == feature.getProperty('id')) {
				foundFeature = feature;
			}
		});
		var thisObject = this;
		
		if(foundFeature) {
			var color = thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureColor(Ljava/lang/String;)(foundFeature.getId());
			geoJsonMap.data.overrideStyle(foundFeature, {
				fillColor: color
			});
		}
	}-*/;
	
	private native void selectProfile(String leveeId, boolean show) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
		var foundFeature = null;
		geoJsonMap.data.forEach(function(feature) {
			if(feature.getProperty('type') == 'profile' && leveeId == feature.getProperty('id')) {
				foundFeature = feature;
			}
		});
		
		if(show) {
			geoJsonMap.data.overrideStyle(foundFeature, {
				strokeWeight: 3
			});
		} else {
			geoJsonMap.data.overrideStyle(foundFeature, {
				strokeWeight: 1
			});
		}
	}-*/;
	
	private native void selectSensor(String sensorId, boolean show) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
		var foundFeature = null;
		geoJsonMap.data.forEach(function(feature) {
			if(feature.getProperty('type') == 'sensor' && sensorId == feature.getProperty('id')) {
				foundFeature = feature;
			}
		});
		
		if(show) {
			geoJsonMap.data.overrideStyle(foundFeature, {
				icon: $wnd.iconBaseUrl + '/sensor-selected.png'
			});
		} else {
			geoJsonMap.data.overrideStyle(foundFeature, {
				icon: $wnd.iconBaseUrl + '/sensor.png'
			});
		}
	}-*/;

	private native Object createLatLngBounds() /*-{
		return new $wnd.google.maps.LatLngBounds();
	}-*/;
	
	private native void extend(Object bounds, Double lat, Double lng) /*-{
		bounds.extend(new $wnd.google.maps.LatLng(lat, lng));
	}-*/;
	
	private native JavaScriptObject showMap(Object bounds) /*-{
		var map = new $wnd.google.maps.Map(
			$doc.getElementById(this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::elementId));
		var thisObject = this;
		map.fitBounds(bounds);
//		map.data.loadGeoJson($wnd.geojsonUrl);
		map.data.loadGeoJson($wnd.sensorUrl);
		map.data.loadGeoJson($wnd.profileUrl);
		map.data.setStyle(function(feature) {
			return {
				strokeColor: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureStrokeColor(Ljava/lang/String;)(feature.getId()),
				fillOpacity: 0.9,
				strokeOpacity: 1.0,
				fillColor: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureColor(Ljava/lang/String;)(feature.getId()),
				strokeWeight: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureStrokeWeight(Ljava/lang/String;)(feature.getId()),
				icon: $wnd.iconBaseUrl + '/sensor.png'
			};
		});
		map.data.addListener('mouseover', function(event) {
			map.data.overrideStyle(event.feature, {
				fillOpacity: 0.6,
				strokeOpacity: 0.5
			});
		});
		map.data.addListener('mouseout', function(event) {
			map.data.overrideStyle(event.feature, {
				fillOpacity: 0.9,
				strokeOpacity: 1.0
			});
		});
		map.data.addListener('click', function(event) {
			thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::onFeatureClicked(Ljava/lang/String;)(event.feature.getId());
		});
		
		var drawingManager = new $wnd.google.maps.drawing.DrawingManager({
			drawingControl: true,
			drawingControlOptions: {
				position: $wnd.google.maps.ControlPosition.TOP_CENTER,
				drawingModes: [
					$wnd.google.maps.drawing.OverlayType.RECTANGLE
				]
			},
			rectangleOptions: {
				fillColor: '#aaaaaa',
				fillOpacity: 0.5,
				strokeWeight: 0
			}
		});
		drawingManager.setMap(map);
		$wnd.google.maps.event.addListener(drawingManager, 'rectanglecomplete', function(r) {
			thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::onAreaSelected(FFFF)(r.getBounds().getNorthEast().lat(),
				r.getBounds().getNorthEast().lng(), r.getBounds().getSouthWest().lat(), r.getBounds().getSouthWest().lng());
			r.setMap(null);
		});
		
		return map;
	}-*/;
	
	private native String getFeatureType(String featureId) /*-{
		if(featureId) {
			var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
			var feature = geoJsonMap.data.getFeatureById(featureId);
			
			if(feature) {
				return feature.getProperty('type');
			}
		} else {
			return null;
		}
	}-*/;
	
	private native String getProfileId(String featureId) /*-{
		if(featureId) {
			var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
			var feature = geoJsonMap.data.getFeatureById(featureId);
			
			if(feature && feature.getProperty('type') == 'profile') {
				return feature.getProperty('id');
			}
		}
	
		return null;
	}-*/;
	
	private native String getSensorId(String featureId) /*-{
		if(featureId) {
			var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
			var feature = geoJsonMap.data.getFeatureById(featureId);
			
			if(feature && feature.getProperty('type') == 'sensor') {
				return feature.getProperty('id');
			}
		}
	
		return null;
	}-*/;
	
	private native void push(double value, String timestamp, JavaScriptObject values) /*-{
		values.push({value: value, timestamp: timestamp});
	}-*/;
	
	private native void showMorrisChart(JavaScriptObject values, String unit, double min, double max, String label) /*-{
		new $wnd.Morris.Area({
			element: 'measurements',
			data: values,
			xkey: 'timestamp',
			ykeys: ['value'],
			labels: [label],
			ymin: min,
			ymax: max,
			yLabelFormat: function(value) {
				return value.toFixed(3) + ' ' + unit;
			}
		});
	}-*/;
	
	private native JavaScriptObject showDygraphChart(String values, String yLabel, String title) /*-{
		return new $wnd.Dygraph($doc.getElementById('measurements'), values, {
			showRangeSelector: true,
			ylabel: yLabel,
			labelsDivStyles: {
				textAlign: 'right'
			},
			axisLabelWidth: 100,
			title: title,
			digitsAfterDecimal: 1,
			delimiter: '|'
		});
	}-*/;
	
	private native void updateDygraphData(String data) /*-{
		var graph = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::currentGraph;
		
		if(graph) {
			this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::currentGraph.updateOptions({
				file: data
			});
		}
	}-*/;
}