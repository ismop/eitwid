package pl.ismop.web.client.widgets.maps.google;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.DapController.MeasurementsCallback;
import pl.ismop.web.client.dap.DapController.SensorCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.measurement.Measurement;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.client.widgets.maps.MapMessages;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
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
	private Map<String, Levee> levees;
	private String detailsElementId;
	private LeveeSummaryPresenter selectedLevee;
	private Object map;
	private Map<String, String> leveeColors;

	@Inject
	public GoogleMapsPresenter(DapController dapController, MapMessages messages) {
		this.dapController = dapController;
		this.messages = messages;
		levees = new HashMap<>();
		leveeColors = new HashMap<>();
		leveeColors.put("none", "#D9EDF7");
		leveeColors.put("heightened", "#FAEBCC");
		leveeColors.put("severe", "#EBCCD1");
	}
	
	public void onDrawGoogleMap(String mapElementId, String detailsElementId) {
		this.elementId = mapElementId;
		this.detailsElementId = detailsElementId;
		showProgressIndicator(true);
		setNoFeatureSelectedLabel();
		dapController.getLevees(new LeveesCallback() {
			@Override
			public void onError(int code, String message) {
				showProgressIndicator(false);
				Window.alert("Error: " + message);
			}
			
			@Override
			public void processLevees(List<Levee> levees) {
				showProgressIndicator(false);
				List<List<Double>> allPoints = new ArrayList<List<Double>>();

				for(Levee levee : levees) {
					GoogleMapsPresenter.this.levees.put(levee.getId(), levee);
					allPoints.addAll(levee.getShape().getCoordinates());
				}
				
				if(allPoints.size() > 1) {
					Object bounds = createLatLngBounds();
					
					for(List<Double> point : allPoints) {
						extend(bounds, point.get(0), point.get(1));
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
	
	public void onLeveeUpdated(Levee newLevee) {
		if(levees.get(newLevee.getId()) != null) {
			levees.put(newLevee.getId(), newLevee);
			updateLeveeOnMap(newLevee.getId());
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
			if(getLeveeId(featureId) != null) {
				Levee levee = levees.get(getLeveeId(featureId));
				showLeveeDetails(levee);
			} else if(getSensorId(featureId) != null) {
				showSensorDetails(getSensorId(featureId));
			}
		}
	}

	private void showSensorDetails(String sensorId) {
		dapController.getSensor(sensorId, new SensorCallback() {
			@Override
			public void onError(int code, String message) {
				setNoFeatureSelectedLabel();
				Window.alert("Error: " + message);
			}

			@Override
			public void processSensor(final Sensor sensor) {
				if(selectedLevee != null) {
					String previousLeveeId = selectedLevee.getLevee().getId();
					eventBus.removeHandler(selectedLevee);
					selectedLevee = null;
					selectLevee(previousLeveeId, false);
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
							
							Element header = DOM.createElement("h4");
							header.setInnerText(sensor.getUnitLabel() + " (" + sensor.getCustomId() + ")");
							RootPanel.get(detailsElementId).getElement().appendChild(header);
							
							Element chart = DOM.createDiv();
							chart.setId("measurements");
							chart.getStyle().setHeight(250, Unit.PX);
							RootPanel.get(detailsElementId).getElement().appendChild(chart);
							showChart(values, sensor.getUnit(), min, max, sensor.getUnitLabel());
						}
					}});
			}
		});
	}

	private void showLeveeDetails(Levee levee) {
		String previousLeveeId = null;
		
		if(selectedLevee != null) {
			previousLeveeId = selectedLevee.getLevee().getId();
			eventBus.removeHandler(selectedLevee);
			selectedLevee = null;
		}
		
		Element element = DOM.getElementById(detailsElementId);
		element.setInnerHTML("");
		
		LeveeSummaryPresenter presenter = eventBus.addHandler(LeveeSummaryPresenter.class);
		selectedLevee = presenter;
		presenter.setLevee(levee);
		RootPanel.get(detailsElementId).add(presenter.getView());
		
		if(previousLeveeId != null) {
			selectLevee(previousLeveeId, false);
		}
		
		selectLevee(levee.getId(), true);
	}
	
	private String getFeatureColor(String featureId) {
		String leveeId = getLeveeId(featureId);
		
		if(leveeId != null) {
			for(Levee levee : levees.values()) {
				if(levee.getId().equals(leveeId)) {
					return leveeColors.get(levee.getEmergencyLevel());
				}
			}
		}
		
		return "white";
	}
	
	private native void updateLeveeOnMap(String leveeId) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
		var foundFeature = null;
		geoJsonMap.data.forEach(function(feature) {
			if(feature.getProperty('type') == 'levee' && leveeId == feature.getProperty('id')) {
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
	
	private native void selectLevee(String leveeId, boolean show) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
		var foundFeature = null;
		geoJsonMap.data.forEach(function(feature) {
			if(feature.getProperty('type') == 'levee' && leveeId == feature.getProperty('id')) {
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

	private native Object createLatLngBounds() /*-{
		return new $wnd.google.maps.LatLngBounds();
	}-*/;
	
	private native void extend(Object bounds, Double lat, Double lng) /*-{
		bounds.extend(new $wnd.google.maps.LatLng(lat, lng));
	}-*/;
	
	private native Object showMap(Object bounds) /*-{
		var map = new $wnd.google.maps.Map(
			$doc.getElementById(this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::elementId));
		var thisObject = this;
		map.fitBounds(bounds);
		map.data.loadGeoJson($wnd.geojsonUrl);
		map.data.loadGeoJson($wnd.sensorUrl);
		map.data.setStyle(function(feature) {
			return {
				strokeColor: 'black',
				fillOpacity: 0.9,
				strokeOpacity: 1.0,
				fillColor: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getFeatureColor(Ljava/lang/String;)(feature.getId()),
				strokeWeight: 1
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
		
		return map;
	}-*/;
	
	private native String getFeatureType(String featureId) /*-{
		if(featureId) {
			var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
			var feature = geoJsonMap.data.getFeatureById(id);
			
			if(feature) {
				return feature.getProperty('type');
			}
		} else {
			return null;
		}
	}-*/;
	
	private native String getLeveeId(String featureId) /*-{
		if(featureId) {
			var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
			var feature = geoJsonMap.data.getFeatureById(featureId);
			
			if(feature && feature.getProperty('type') == 'levee') {
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
	
	private native void showChart(JavaScriptObject values, String unit, double min, double max, String label) /*-{
		new $wnd.Morris.Area({
			element: 'measurements',
			data: values,
			xkey: 'timestamp',
			ykeys: ['value'],
			labels: [label],
			ymin: min,
			ymax: max,
			postUnits: ' ' + unit
		});
	}-*/;
}