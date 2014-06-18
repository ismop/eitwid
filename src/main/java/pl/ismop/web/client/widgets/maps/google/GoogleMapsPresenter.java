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
					GoogleMapsPresenter.this.levees.put(levee.getName(), levee);
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
			String previousLeveeName = null;
			
			if(selectedLevee != null) {
				previousLeveeName = selectedLevee.getLevee().getName();
				eventBus.removeHandler(selectedLevee);
				selectedLevee = null;
			}
			
			Element element = DOM.getElementById(detailsElementId);
			element.setInnerHTML("");
			
			LeveeSummaryPresenter presenter = eventBus.addHandler(LeveeSummaryPresenter.class);
			selectedLevee = presenter;
			presenter.setLevee(levee);
			RootPanel.get(detailsElementId).add(presenter.getView());
			
			if(previousLeveeName != null) {
				selectLevee(previousLeveeName, false);
			}
			
			selectLevee(levee.getName(), true);
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
	
	private native void updateLeveeOnMap(String id) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
		var feature = geoJsonMap.data.getFeatureById(id);
		var thisObject = this;
		
		if(feature) {
			var color = thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getLeveeColor(Ljava/lang/String;)(id);
			geoJsonMap.data.overrideStyle(feature, {
				fillColor: color
			});
		}
	}-*/;
	
	private native void selectLevee(String leveeName, boolean show) /*-{
		var geoJsonMap = this.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::map;
		var foundFeature = null;
		geoJsonMap.data.forEach(function(feature) {
			if(leveeName == feature.getProperty('name')) {
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
		map.data.setStyle(function(feature) {
			return {
				strokeColor: 'black',
				fillOpacity: 0.9,
				strokeOpacity: 1.0,
				fillColor: thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::getLeveeColor(Ljava/lang/String;)(feature.getId()),
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
			thisObject.@pl.ismop.web.client.widgets.maps.google.GoogleMapsPresenter::showLeveeDetails(Ljava/lang/String;)(event.feature.getProperty('name'));
		});
		
		return map;
	}-*/;
}