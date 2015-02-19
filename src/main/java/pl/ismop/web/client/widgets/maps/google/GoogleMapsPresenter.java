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
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.DapController.SensorCallback;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.widgets.maps.MapMessages;
import pl.ismop.web.client.widgets.newexperiment.ExperimentPresenter;
import pl.ismop.web.client.widgets.popup.PopupPresenter;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class GoogleMapsPresenter extends BaseEventHandler<MainEventBus> {
	private static final Logger log = LoggerFactory.getLogger(GoogleMapsPresenter.class);
	
	private DapController dapController;
	private String elementId;
	private MapMessages messages;
	private Map<String, Section> profiles;
	private ProfilePresenter selectedProfile;
	private JavaScriptObject map;
	private Map<String, String> profileColors;
	private Sensor selectedSensor;
	private JavaScriptObject currentGraph;
	private Timer sensorTimer;
	private ListBox days;
	private JavaScriptObject profileMapData;
	private JavaScriptObject sensorMapData;
	private PopupPresenter popupPresenter;

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
	
	public void onDrawGoogleMap(String mapElementId) {
		this.elementId = mapElementId;
		showProgressIndicator(true);
		dapController.getSections(new SectionsCallback() {
			@Override
			public void onError(int code, String message) {
				showProgressIndicator(false);
				Window.alert("Error: " + message);
			}

			@Override
			public void processSections(List<Section> sections) {
				showProgressIndicator(false);
				List<List<Double>> allPoints = new ArrayList<List<Double>>();

				for(Section profile : sections) {
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
	
	public void onProfileUpdated(Section profile) {
		if(profiles.get(profile.getId()) != null) {
			profiles.put(profile.getId(), profile);
			updateProfileOnMap(profile.getId());
		}
	}
	
	public void onShowExperiments(List<String> experimentIds) {
		onPopupClosed();
	}
	
	public void onShowLevees(boolean show) {
		showLayer(profileMapData, show);
	}
	
	public void onShowSensors(boolean show) {
		showLayer(sensorMapData, show);
	}
	
	public void onPopupClosed() {
		if(selectedProfile != null) {
			String previousProfileId = selectedProfile.getProfile().getId();
			eventBus.removeHandler(selectedProfile);
			selectedProfile.stopUpdate();
			selectedProfile = null;
			selectProfile(previousProfileId, false);
		}
		
		if(sensorTimer != null) {
			sensorTimer.cancel();
			sensorTimer = null;
		}
		
		if(selectedSensor != null) {
			selectSensor(selectedSensor.getId(), false);
		}
		
		selectedSensor = null;
		currentGraph = null;
	}

	private void setNoMeasurementsLabel(String sensorId) {
		eventBus.setTitleAndShow(messages.sensorTitle(sensorId), new Label(messages.noMeasurements()));
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
		
		if(getProfileId(featureId) != null) {
			Section profile = profiles.get(getProfileId(featureId));
			showProfileDetails(profile);
		} else if(getSensorId(featureId) != null) {
			showSensorDetails(getSensorId(featureId));
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
						Window.alert("Error: " + message);
					}

					@Override
					public void processMeasurements(List<Measurement> measurements) {
						if(selectedSensor != null) {
							selectSensor(selectedSensor.getId(), false);
						}
						
						selectedSensor = sensor;
						selectSensor(selectedSensor.getId(), true);
						
						if(measurements.size() == 0) {
							setNoMeasurementsLabel(sensor.getId());
						} else {
							
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
							
							Element chart = DOM.createDiv();
							chart.setId("measurements");
							chart.getStyle().setHeight(250, Unit.PX);
							chart.getStyle().setWidth(600, Unit.PX);
							chart.getStyle().setBackgroundColor("white");
							
							FlowPanel panel = new FlowPanel();
							panel.getElement().appendChild(chart);
							eventBus.setTitleAndShow(messages.sensorTitle(sensor.getCustomId()), panel);
							
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
				if(selectedSensor != null) {
					String data = getDygraphValues(measurements, selectedSensor.getUnitLabel());
					updateDygraphData(data);
					sensorTimer = new Timer() {
						@Override
						public void run() {
							updateSensorDetails(sensorId);
						}
					};
					sensorTimer.schedule(10000);
				}
			}});
	}
	
	private String getDygraphValues(List<Measurement> measurements, String yLabel) {
		StringBuilder builder = new StringBuilder();
		builder.append("aa|").append(yLabel).append("\n");
		
		for(Measurement measurement : measurements) {
			DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
			Date date = format.parse(measurement.getTimestamp());
			date = new Date(date.getTime() - 7200000);
			builder.append(DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).format(date))
					.append("|")
					.append(measurement.getValue())
					.append("\n");
		}
		
		return builder.toString();
	}

	private void showProfileDetails(Section profile) {
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
		
		ProfilePresenter presenter = eventBus.addHandler(ProfilePresenter.class);
		selectedProfile = presenter;
		presenter.setProfile(profile);
		eventBus.setTitleAndShow(messages.profileTitle(profile.getId()), presenter.getView());
		
		if(previousProfileId != null) {
			selectProfile(previousProfileId, false);
		}
		
		selectProfile(profile.getId(), true);
	}

	private String getFeatureColor(String featureId) {
		String profileId = getProfileId(featureId);
		
		if(profileId != null) {
			for(Section profile : profiles.values()) {
				if(profile.getId().equals(profileId)) {
					String color = profileColors.get(profile.getThreatLevel());
					
					return color == null ? "#aaaaaa" : color;
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
		var profileData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData;
		var foundFeature = null;
		profileData.forEach(function(feature) {
			if(feature.getProperty('type') == 'profile' && leveeId == feature.getProperty('id')) {
				foundFeature = feature;
			}
		});
		var thisObject = this;
		
		if(foundFeature) {
			var color = thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureColor(Ljava/lang/String;)(foundFeature.getId());
			profileData.overrideStyle(foundFeature, {
				fillColor: color
			});
		}
	}-*/;
	
	private native void selectProfile(String leveeId, boolean show) /*-{
		var profileData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData;
		var foundFeature = null;
		profileData.forEach(function(feature) {
			if(feature.getProperty('type') == 'profile' && leveeId == feature.getProperty('id')) {
				foundFeature = feature;
			}
		});
		
		if(show) {
			profileData.overrideStyle(foundFeature, {
				strokeWeight: 3
			});
		} else {
			profileData.overrideStyle(foundFeature, {
				strokeWeight: 1
			});
		}
	}-*/;
	
	private native void selectSensor(String sensorId, boolean show) /*-{
		var sensorData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sensorMapData;
		var foundFeature = null;
		sensorData.forEach(function(feature) {
			if(feature.getProperty('type') == 'sensor' && sensorId == feature.getProperty('id')) {
				foundFeature = feature;
			}
		});
		
		if(show) {
			sensorData.overrideStyle(foundFeature, {
				icon: $wnd.iconBaseUrl + '/sensor-selected.png'
			});
		} else {
			sensorData.overrideStyle(foundFeature, {
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
		
		var profileData = new $wnd.google.maps.Data();
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData = profileData;
		profileData.loadGeoJson($wnd.profileUrl);
		profileData.setMap(map);
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::overrideStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(profileData);
		
		var sensorData = new $wnd.google.maps.Data();
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sensorMapData = sensorData;
		sensorData.loadGeoJson($wnd.sensorUrl);
		//sensors are not shown at the start
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::overrideStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(sensorData);
		
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
	
	private native void overrideStyle(JavaScriptObject mapData) /*-{
		var thisObject = this;
		mapData.setStyle(function(feature) {
			return {
				strokeColor: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureStrokeColor(Ljava/lang/String;)(feature.getId()),
				fillOpacity: 0.9,
				strokeOpacity: 1.0,
				fillColor: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureColor(Ljava/lang/String;)(feature.getId()),
				strokeWeight: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureStrokeWeight(Ljava/lang/String;)(feature.getId()),
				icon: $wnd.iconBaseUrl + '/sensor.png'
			};
		});
		mapData.addListener('mouseover', function(event) {
			mapData.overrideStyle(event.feature, {
				fillOpacity: 0.6,
				strokeOpacity: 0.5
			});
		});
		mapData.addListener('mouseout', function(event) {
			mapData.overrideStyle(event.feature, {
				fillOpacity: 0.9,
				strokeOpacity: 1.0
			});
		});
		mapData.addListener('click', function(event) {
			thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::onFeatureClicked(Ljava/lang/String;)(event.feature.getId());
		});
	}-*/;
	
	private native String getFeatureType(String featureId) /*-{
		if(featureId) {
			var feature = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData.getFeatureById(featureId);
			
			if(feature == null) {
				feature = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sensorMapData.getFeatureById(featureId);
			}
			
			if(feature) {
				return feature.getProperty('type');
			}
		} else {
			return null;
		}
	}-*/;
	
	private native String getProfileId(String featureId) /*-{
		if(featureId) {
			var profileData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData;
			var feature = profileData.getFeatureById(featureId);
			
			if(feature && feature.getProperty('type') == 'profile') {
				return feature.getProperty('id');
			}
		}
	
		return null;
	}-*/;
	
	private native String getSensorId(String featureId) /*-{
		if(featureId) {
			var sensorData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sensorMapData;
			var feature = sensorData.getFeatureById(featureId);
			
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
	
	private native void showLayer(JavaScriptObject mapData, boolean show) /*-{
		if(show) {
			mapData.setMap(this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map);
		} else {
			mapData.setMap(null);
		}
	}-*/;
}