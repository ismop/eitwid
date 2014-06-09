package pl.ismop.web.client.widgets.maps.ol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.DapController.LeveesCallback;
import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.widgets.maps.MapMessages;
import pl.ismop.web.client.widgets.summary.LeveeSummaryPresenter;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.EventHandler;
import com.mvp4g.client.event.BaseEventHandler;

@EventHandler
public class OpenLayersMapsPresenter extends BaseEventHandler<MainEventBus> {
	private static final Logger log = LoggerFactory.getLogger(OpenLayersMapsPresenter.class);
	
	private DapController dapController;
	private String elementId;
	private MapMessages messages;
	private Map<String, Levee> levees;
	private String detailsElementId;
	private LeveeSummaryPresenter selectedLevee;
	private Object map;
	private Map<String, String> leveeColors;

	@Inject
	public OpenLayersMapsPresenter(DapController dapController, MapMessages messages) {
		this.dapController = dapController;
		this.messages = messages;
		levees = new HashMap<>();
		leveeColors = new HashMap<>();
		leveeColors.put("none", "#D9EDF7");
		leveeColors.put("heightened", "#FAEBCC");
		leveeColors.put("severe", "#EBCCD1");
	}
	
	public void onDrawOpenLayersMap(String mapElementId, String detailsElementId) {
		this.elementId = mapElementId;
		this.detailsElementId = detailsElementId;
		showProgressIndicator(true);
		setNoLeveeSelectedLabel();
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
					OpenLayersMapsPresenter.this.levees.put(levee.getName(), levee);
					allPoints.addAll(levee.getShape().getCoordinates());
				}
				
				if(allPoints.size() > 1) {
					Object bounds = createLatLngBounds();
					
					for(List<Double> point : allPoints) {
						extend(bounds, point.get(0), point.get(1));
					}
					
					Element element = DOM.getElementById(OpenLayersMapsPresenter.this.elementId);
					
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
		if(levees.get(newLevee.getName()) != null) {
			levees.put(newLevee.getName(), newLevee);
			updateLeveeOnMap(newLevee.getId());
		}
	}

	private void setNoLeveeSelectedLabel() {
		Element element = DOM.getElementById(detailsElementId);
		
		if(element != null) {
			element.setInnerText(messages.noLeveeSelected());
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
	
	private  void showLeveeDetails(String leveeName) {
		log.info("Showing levee details for name {}", leveeName);
		
		Levee levee = levees.get(leveeName);
		
		if(levee != null && RootPanel.get(detailsElementId) != null) {
			if(selectedLevee != null) {
				eventBus.removeHandler(selectedLevee);
				selectedLevee = null;
			}
			
			Element element = DOM.getElementById(detailsElementId);
			element.setInnerHTML("");
			
			LeveeSummaryPresenter presenter = eventBus.addHandler(LeveeSummaryPresenter.class);
			selectedLevee = presenter;
			presenter.setLevee(levee);
			RootPanel.get(detailsElementId).add(presenter.getView());
		}
	}
	
	private String getLeveeColor(String id) {
		for(Levee levee : levees.values()) {
			if(levee.getId().equals(id)) {
				return leveeColors.get(levee.getEmergencyLevel());
			}
		}
		
		return "white";
	}
	
	private String getLeveeWidth(String id) {
		if(selectedLevee != null) {
			if(selectedLevee.getLevee().getId().equals(id)) {
				return "3";
			}
		}
		
		return "1";
	}
	
	private native void updateLeveeOnMap(String id) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter::map;
		var feature = geoJsonMap.getLayersByName('GeoJSON')[0].getFeatureByFid(id);
		var thisObject = this;
		
		if(feature) {
			var color = thisObject.@pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter::getLeveeColor(Ljava/lang/String;)(id);
			geoJsonMap.getLayersByName('GeoJSON')[0].drawFeature(feature, {
				fillColor: color,
				strokeWidth: thisObject.@pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter::getLeveeWidth(Ljava/lang/String;)(id)
			});
		}
	}-*/;

	private native Object createLatLngBounds() /*-{
		return new $wnd.OpenLayers.Bounds();
	}-*/;
	
	private native void extend(Object bounds, Double lat, Double lng) /*-{
		bounds.extend(new $wnd.OpenLayers.LonLat(lng, lat));
	}-*/;
	
	private native Object showMap(Object bounds) /*-{
		var map = new $wnd.OpenLayers.Map(this.@pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter::elementId, {
			maxScale: 1000
		});
		var wms = new $wnd.OpenLayers.Layer.WMS('OpenLayers WMS', $wnd.mapServerBaseUrl, {
			layers:'basic'
		});
		map.addLayer(wms);
		map.zoomToExtent(bounds);
		
		var thisObject = this;
		var defaultStyle = new $wnd.OpenLayers.Style({
			fillOpacity: 0.9,
			strokeOpacity: 1.0,
			strokeWidth: 1,
			strokeColor: 'black',
			fillColor: "${getColor}"
		}, {
			context: {
				getColor: function(feature) {
					var color = thisObject.@pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter::getLeveeColor(Ljava/lang/String;)(feature.fid);
					return color;
				}
			}
		});
		var selectStyle = new $wnd.OpenLayers.Style({
			strokeWidth: 3
		});
		var hoverStyle = new $wnd.OpenLayers.Style({
			fillOpacity: 0.6,
			strokeOpacity: 0.5,
			strokeWidth: '${getWidth}'
		}, {
			context: {
				getWidth: function(feature) {
					var width = thisObject.@pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter::getLeveeWidth(Ljava/lang/String;)(feature.fid);
					return width;
				}
			}
		});
		var styleMap = new $wnd.OpenLayers.StyleMap({
			'default': defaultStyle,
			'select': selectStyle,
			'temporary': hoverStyle
        });
		
		var geojsonLayer = new $wnd.OpenLayers.Layer.Vector('GeoJSON', {
            strategies: [new $wnd.OpenLayers.Strategy.Fixed()],
            protocol: new $wnd.OpenLayers.Protocol.HTTP({
                url: $wnd.geojsonUrl,
                format: new $wnd.OpenLayers.Format.GeoJSON()
            }),
            styleMap: styleMap
        });
        map.addLayer(geojsonLayer);
        
        var hoverControl = new $wnd.OpenLayers.Control.SelectFeature(geojsonLayer, {
			hover: true,
			highlightOnly: true,
			renderIntent: 'temporary'
		});
		map.addControl(hoverControl);
		hoverControl.activate();
        
        var clickControl = new $wnd.OpenLayers.Control.SelectFeature(geojsonLayer, {
        	clickout: true,
			onSelect: function(feature) {
				thisObject.@pl.ismop.web.client.widgets.maps.ol.OpenLayersMapsPresenter::showLeveeDetails(Ljava/lang/String;)(feature.attributes['name']);
			}
		});
		map.addControl(clickControl);
		clickControl.activate();
		
		return map;
	}-*/;
}