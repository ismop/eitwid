package pl.ismop.web.client.widgets.maps.google;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.DeviceAggregationsCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.SectionsCallback;
import pl.ismop.web.client.dap.DapController.SensorCallback;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
import pl.ismop.web.client.dap.levee.PolygonShape;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.geojson.GeoJsonFeature;
import pl.ismop.web.client.geojson.GeoJsonFeatures;
import pl.ismop.web.client.geojson.GeoJsonFeaturesEncDec;
import pl.ismop.web.client.geojson.LineGeometry;
import pl.ismop.web.client.geojson.PointGeometry;
import pl.ismop.web.client.widgets.maps.MapMessages;
import pl.ismop.web.client.widgets.newexperiment.ThreatAssessmentPresenter;
import pl.ismop.web.client.widgets.sideprofile.SideProfilePresenter;

@EventHandler
public class GoogleMapsPresenter extends BaseEventHandler<MainEventBus> {
	private static final Logger log = LoggerFactory.getLogger(GoogleMapsPresenter.class);
	
	private DapController dapController;
	private String elementId;
	private MapMessages messages;
	private Map<String, Section> sections;
	private String selectedSectionId;
	private JavaScriptObject map;
	private Map<String, String> sectionColors;
	private Sensor selectedSensor;
	private JavaScriptObject currentGraph;
	private Timer sensorTimer;
	private ListBox days;
	private JavaScriptObject sectionMapData;
	private JavaScriptObject sensorMapData;
	private JavaScriptObject profileMapData;
	private SideProfilePresenter presenter;
	private GeoJsonFeaturesEncDec jsonFeaturesEncDec;
	private List<String> profileFeatureIds;
	private Integer featureIdGenerator;
	private List<String> aggregateFeatureIds;

	@Inject
	public GoogleMapsPresenter(DapController dapController, MapMessages messages, GeoJsonFeaturesEncDec jsonFeaturesEncDec) {
		this.dapController = dapController;
		this.messages = messages;
		this.jsonFeaturesEncDec = jsonFeaturesEncDec;
		sections = new HashMap<>();
		sectionColors = new HashMap<>();
		sectionColors.put("none", "#D9EDF7");
		sectionColors.put("heightened", "#FAEBCC");
		sectionColors.put("severe", "#EBCCD1");
		profileFeatureIds = new ArrayList<>();
		featureIdGenerator = new Random().nextInt();
		aggregateFeatureIds = new ArrayList<>();
	}
	
	public void onDrawGoogleMap(String mapElementId, String leveeId) {
		this.elementId = mapElementId;
		showProgressIndicator(true);
		dapController.getSections(leveeId, new SectionsCallback() {
			@Override
			public void onError(int code, String message) {
				showProgressIndicator(false);
				Window.alert("Error: " + message);
			}

			@Override
			public void processSections(List<Section> sections) {
				showProgressIndicator(false);
				List<List<Double>> allPoints = new ArrayList<List<Double>>();

				for(Section section : sections) {
					GoogleMapsPresenter.this.sections.put(section.getId(), section);
					allPoints.addAll(section.getShape().getCoordinates());
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
		if(sections.get(profile.getId()) != null) {
			sections.put(profile.getId(), profile);
			updateSectionOnMap(profile.getId());
		}
	}
	
	public void onShowExperiments(List<String> experimentIds) {
		onPopupClosed();
	}
	
	public void onShowLevees(boolean show) {
		showLayer(sectionMapData, show);
	}
	
	public void onShowSensors(boolean show) {
		showLayer(sensorMapData, show);
	}
	
	public void onPopupClosed() {
		if(selectedSectionId != null) {
			selectSection(selectedSectionId, false);
			selectedSectionId = null;
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
	
	public void onZoomToAndSelectSection(String sectionId) {
		if(sections.containsKey(sectionId)) {
			Object bounds = createLatLngBounds();
			
			for(List<Double> point : sections.get(sectionId).getShape().getCoordinates()) {
				extend(bounds, point.get(1), point.get(0));
			}
			
			panMap(bounds);
			showSectionDetails(sections.get(sectionId));
		}
	}
	
	public void onZoomToLevee(String selectedLeveeId) {
		if(selectedSectionId != null) {
			selectSection(selectedSectionId, false);
			selectedSectionId = null;
		}
		
		Object bounds = createLatLngBounds();
		
		for(Section section : sections.values()) {
			for(List<Double> point : section.getShape().getCoordinates()) {
				extend(bounds, point.get(1), point.get(0));
			}
		}
		
		panMap(bounds);
	}
	
	public void onDrawProfiles(Map<String, PolygonShape> profileShapes) {
		if(profileShapes.size() > 0) {
			if(profileFeatureIds.size() > 0) {
				for(String profileFeatureId : profileFeatureIds) {
					removeProfileFeature(profileFeatureId);
				}
				
				profileFeatureIds.clear();
			}
			
			List<GeoJsonFeature> features = new ArrayList<>();
			
			for(String profileId : profileShapes.keySet()) {
				PolygonShape shape = profileShapes.get(profileId);
				LineGeometry lineGeometry = new LineGeometry();
				lineGeometry.setCoordinates(shape.getCoordinates());
				
				GeoJsonFeature feature = new GeoJsonFeature();
				feature.setGeometry(lineGeometry);
				feature.setId(String.valueOf(featureIdGenerator++));
				feature.setProperties(new HashMap<String, String>());
				feature.getProperties().put("id", profileId);
				feature.getProperties().put("name", feature.getId());
				feature.getProperties().put("type", "profile");
				profileFeatureIds.add(feature.getId());
				features.add(feature);
			}
			
			String jsonValue = jsonFeaturesEncDec.encode(new GeoJsonFeatures(features)).toString();
			addFeatures(jsonValue);
		}
	}
	
	public void onMarkAndCompleteProfile(String profileId) {
		dapController.getDeviceAggregations(profileId, new DeviceAggregationsCallback() {
			@Override
			public void onError(int code, String message) {
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processDeviceAggregations(List<DeviceAggregation> deviceAggreagations) {
				if(deviceAggreagations.size() > 0) {
					List<GeoJsonFeature> features = new ArrayList<>();
					
					for(DeviceAggregation deviceAggregation : deviceAggreagations) {
						
						PointGeometry pointGeometry = new PointGeometry();
						pointGeometry.setCoordinates(deviceAggregation.getPlacement().getCoordinates());
						
						GeoJsonFeature feature = new GeoJsonFeature();
						feature.setGeometry(pointGeometry);
						feature.setId(String.valueOf(featureIdGenerator++));
						feature.setProperties(new HashMap<String, String>());
						feature.getProperties().put("id", deviceAggregation.getId());
						feature.getProperties().put("name", feature.getId());
						feature.getProperties().put("type", "aggregate");
						aggregateFeatureIds.add(feature.getId());
						features.add(feature);
					}
					
					String jsonValue = jsonFeaturesEncDec.encode(new GeoJsonFeatures(features)).toString();
					addFeatures(jsonValue);
				}
			}
		});
	}
	
	public void onDeselectSection() {
		if(selectedSectionId != null) {
			selectSection(selectedSectionId, false);
			selectedSectionId = null;
		}
	}
	
	public void onRemoveProfiles() {
		if(profileFeatureIds.size() > 0) {
			for(String profileFeatureId : profileFeatureIds) {
				removeProfileFeature(profileFeatureId);
			}
			
			profileFeatureIds.clear();
		}
	}
	
	public void onRemoveProfileAggregates() {
		if(aggregateFeatureIds.size() > 0) {
			for(String aggregateFeatureId : aggregateFeatureIds) {
				removeAggregateFeature(aggregateFeatureId);
			}
			
			aggregateFeatureIds.clear();
		}
	}

	private void setNoMeasurementsLabel(String sensorId) {
		eventBus.setTitleAndShow(messages.sensorTitle(sensorId), new Label(messages.noMeasurements()), false);
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
		
		if(getSectionId(featureId) != null) {
			onZoomToAndSelectSection(getSectionId(featureId));
			eventBus.sectionSelectedOnMap(selectedSectionId);
		} else if(getSensorId(featureId) != null) {
			showSensorDetails(getSensorId(featureId));
		} else if(getProfileId(featureId) != null) {
			eventBus.profilePicked(getProfileId(featureId));
		} else if(getAggregateId(featureId) != null) {
			eventBus.aggregatePicked(getAggregateId(featureId));
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
				if(selectedSectionId != null) {
					selectSection(selectedSectionId, false);
					selectedSectionId = null;
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
							eventBus.setTitleAndShow(messages.sensorTitle(sensor.getCustomId()), panel, false);
							
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

	private void showSectionDetails(Section section) {
		if(sensorTimer != null) {
			sensorTimer.cancel();
			sensorTimer = null;
		}
		
		if(selectedSensor != null) {
			selectSensor(selectedSensor.getId(), false);
		}
		
		selectedSensor = null;
		currentGraph = null;
		
		String previousSectionId = null;
		
		if(selectedSectionId != null) {
			previousSectionId = selectedSectionId;
		}
		
		selectedSectionId = section.getId();
		
		if(previousSectionId != null) {
			selectSection(previousSectionId, false);
		}
		
		selectSection(selectedSectionId, true);
	}

	private String getFeatureColor(String featureId) {
		String sectionId = getSectionId(featureId);
		
		if(sectionId != null) {
			for(Section section : sections.values()) {
				if(section.getId().equals(sectionId)) {
					String color = sectionColors.get(null);
					
					return color == null ? "#aaaaaa" : color;
				}
			}
		}
		
		return "white";
	}
	
	private String getFeatureIcon(String featureId) {
		if(getFeatureType(featureId).equals("aggregate")) {
			return "/icons/aggregate.png";
		} else {
			return "/icons/sensor.png";
		}
	}
	
	private String getFeatureStrokeColor(String featureId) {
		switch(getFeatureType(featureId)) {
			case "levee":
				return "black";
			case "section":
				return "green";
			case "profile":
				return "#ffe871";
		}
		
		return "white";
	}
	
	private int getFeatureStrokeWeight(String featureId) {
		switch(getFeatureType(featureId)) {
			case "levee":
				return 1;
			case "section":
				return 1;
			case "profile":
				return 7;
		}
		
		return 0;
	}
	
	private void onAreaSelected(float top, float left, float bottom, float right) {
		eventBus.addHandler(ThreatAssessmentPresenter.class);
		eventBus.areaSelected(top, left, bottom, right);
	}
	
	private native void updateSectionOnMap(String leveeId) /*-{
		var profileData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sectionMapData;
		var foundFeature = null;
		profileData.forEach(function(feature) {
			if(feature.getProperty('type') == 'section' && leveeId == feature.getProperty('id')) {
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
	
	private native void selectSection(String sectionId, boolean show) /*-{
		var profileData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sectionMapData;
		var foundFeature = null;
		profileData.forEach(function(feature) {
			if(feature.getProperty('type') == 'section' && sectionId == feature.getProperty('id')) {
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
		var map = new $wnd.google.maps.Map($doc.getElementById(this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::elementId), {
			disableDefaultUI: true,
			draggable: false,
			keyboardShortcuts: false,
			disableDoubleClickZoom: false,
			scrollwheel: true
		});
		
		var thisObject = this;
		map.fitBounds(bounds);
		
		var sectionData = new $wnd.google.maps.Data();
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sectionMapData = sectionData;
		sectionData.loadGeoJson($wnd.profileUrl);
		sectionData.setMap(map);
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::overrideStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(sectionData);
		
		var sensorData = new $wnd.google.maps.Data();
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sensorMapData = sensorData;
		sensorData.loadGeoJson($wnd.sensorUrl);
		//sensors are not shown at the start
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::overrideStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(sensorData);
		
		var profileData = new $wnd.google.maps.Data();
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData = profileData;
		profileData.setMap(map);
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::overrideStyle(Lcom/google/gwt/core/client/JavaScriptObject;)(profileData);
		
//		var drawingManager = new $wnd.google.maps.drawing.DrawingManager({
//			drawingControl: true,
//			drawingControlOptions: {
//				position: $wnd.google.maps.ControlPosition.TOP_CENTER,
//				drawingModes: [
//					$wnd.google.maps.drawing.OverlayType.RECTANGLE
//				]
//			},
//			rectangleOptions: {
//				fillColor: '#aaaaaa',
//				fillOpacity: 0.5,
//				strokeWeight: 0
//			}
//		});
//		drawingManager.setMap(map);
//		$wnd.google.maps.event.addListener(drawingManager, 'rectanglecomplete', function(r) {
//			thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::onAreaSelected(FFFF)(r.getBounds().getNorthEast().lat(),
//				r.getBounds().getNorthEast().lng(), r.getBounds().getSouthWest().lat(), r.getBounds().getSouthWest().lng());
//			r.setMap(null);
//		});
		
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
				icon: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureIcon(Ljava/lang/String;)(feature.getId())
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
			var feature = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sectionMapData.getFeatureById(featureId);
			
			if(feature == null) {
				feature = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sensorMapData.getFeatureById(featureId);
			}
			
			if(feature == null) {
				feature = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData.getFeatureById(featureId);
			}
			
			if(feature) {
				return feature.getProperty('type');
			}
		} else {
			return null;
		}
	}-*/;
	
	private native String getSectionId(String featureId) /*-{
		if(featureId) {
			var profileData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::sectionMapData;
			var feature = profileData.getFeatureById(featureId);
			
			if(feature && feature.getProperty('type') == 'section') {
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
	
	private native void panMap(Object bounds) /*-{
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map.fitBounds(bounds);
	}-*/;
	
	private native void addFeatures(String jsonValue) /*-{
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData.addGeoJson(JSON.parse(jsonValue));
	}-*/;
	
	private native void removeProfileFeature(String profileFeatureId) /*-{
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData.remove(
				this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData.getFeatureById(profileFeatureId));
	}-*/;
	
	private native void removeAggregateFeature(String aggregateFeatureId) /*-{
		this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData.remove(
				this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData.getFeatureById(aggregateFeatureId));
	}-*/;
	
	private native String getAggregateId(String featureId) /*-{
		if(featureId) {
			var aggregateData = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::profileMapData;
			var feature = aggregateData.getFeatureById(featureId);
			
			if(feature && feature.getProperty('type') == 'aggregate') {
				return feature.getProperty('id');
			}
		}
	
		return null;
	}-*/;
}