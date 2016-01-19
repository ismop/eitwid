package pl.ismop.web.client.widgets.common.map;

import static org.gwtbootstrap3.client.ui.constants.ButtonSize.SMALL;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

import pl.ismop.web.client.widgets.common.map.IMapView.IMapPresenter;

public class MapView extends Composite implements IMapView, ReverseViewInterface<IMapPresenter> {
	private static MapViewUiBinder uiBinder = GWT.create(MapViewUiBinder.class);

	interface MapViewUiBinder extends UiBinder<Widget, MapView> {}
	
	private IMapPresenter presenter;
	
	private String elementId;
	
	private JavaScriptObject map, layer, infoWindow;
	
	private boolean initialized;

	private Set<String> selected = new HashSet<>();
	private Set<String> highlighted = new HashSet<>();

	@UiField
	FlowPanel panel, loadingPanel, mapContainer;
	
	public MapView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		elementId = "map-" + hashCode();
		mapContainer.getElement().setAttribute("id", elementId);
	}
	
	@Override
	public void setPresenter(IMapPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IMapPresenter getPresenter() {
		return presenter;
	}
	
	@Override
	protected void onLoad() {
		if(!initialized) {
			initMap();
			
			if(getPresenter().isHoverListeners()) {
				addHoverHandlers();
			}
			
			if(getPresenter().isClickListeners()) {
				addClickListeners();
			}
			
			initialized = true;
		}
	}

	@Override
	public void adjustBounds(List<List<Double>> points) {
		if(points.size() > 1) {
			JavaScriptObject bounds = createLatLngBounds();
			
			for(List<Double> point : points) {
				extendBounds(bounds, point.get(1), point.get(0));
			}
			
			applyBounds(bounds);
		}
	}
	
	@Override
	public void addButton(final String id, String label) {
		Button button = new Button(label);
		button.setSize(SMALL);
		DOM.sinkEvents(button.getElement(), Event.ONCLICK);
		DOM.setEventListener(button.getElement(), new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				if(DOM.eventGetType(event) == Event.ONCLICK) {
					getPresenter().onZoomOut(id);
				}
			}
		});
		addMiddleButton(button.getElement());
	}

	@Override
	public void showLoadingPanel(boolean show) {
		loadingPanel.setVisible(show);
	}

	@Override
	public native void removeButton(String id) /*-{
		this.@pl.ismop.web.client.widgets.common.map.MapView::map.controls[$wnd.google.maps.ControlPosition.TOP_RIGHT].clear();
	}-*/;

	@Override
	public native void removeFeature(String featureId) /*-{
		var feature = this.@pl.ismop.web.client.widgets.common.map.MapView::layer.getFeatureById(featureId);
		
		if(feature) {
			this.@pl.ismop.web.client.widgets.common.map.MapView::layer.remove(feature);
		}
	}-*/;

	@Override
	public native void highlight(String featureId, boolean highlight) /*-{
		var feature = this.@pl.ismop.web.client.widgets.common.map.MapView::layer.getFeatureById(featureId);
		
		if(feature) {
            if(highlight) {
                var thisObject = this;
                var icon = {
                    anchor: {
                        x: 8,
                        y: 8
                    },
                    url: thisObject.@pl.ismop.web.client.widgets.common.map.MapView::getHighlightedFeatureIcon(Ljava/lang/String;)(featureId)
                };
				this.@pl.ismop.web.client.widgets.common.map.MapView::layer.overrideStyle(feature, {
					fillOpacity: 1.0,
					strokeOpacity: 1.0,
					icon: icon
				});
                this.@pl.ismop.web.client.widgets.common.map.MapView::addHighlighted(Ljava/lang/String;)(featureId);
			} else {
                this.@pl.ismop.web.client.widgets.common.map.MapView::removeHighlighted(Ljava/lang/String;)(featureId);
                if(this.@pl.ismop.web.client.widgets.common.map.MapView::isSelected(Ljava/lang/String;)(featureId)) {
                    this.@pl.ismop.web.client.widgets.common.map.MapView::selectFeature(Ljava/lang/String;Z)(featureId, true);
                } else {
                    this.@pl.ismop.web.client.widgets.common.map.MapView::layer.revertStyle(feature);
                }
			}
		}
	}-*/;

	private void addHighlighted(String featureId) {
		highlighted.add(featureId);
	}

	private void removeHighlighted(String featureId) {
		highlighted.remove(featureId);
	}

	private boolean isHighlighted(String featureId) {
		return highlighted.contains(featureId);
	}

	private void addSelected(String featureId) {
		selected.add(featureId);
	}

	private void removeSelected(String featureId) {
		selected.remove(featureId);
	}

	private boolean isSelected(String featureId) {
		return selected.contains(featureId);
	}



	@Override
	public native void addGeoJson(String geoJsonValue) /*-{
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer.addGeoJson(JSON.parse(geoJsonValue));
	}-*/;

	@Override
	public native void selectFeature(String featureId, boolean select) /*-{
		var feature = this.@pl.ismop.web.client.widgets.common.map.MapView::layer.getFeatureById(featureId);
		
		if(feature) {
			if(select) {
				var thisObject = this;
				var icon = {
					anchor: {
						x: 6,
						y: 6
					},
					url: thisObject.@pl.ismop.web.client.widgets.common.map.MapView::getSelectedFeatureIcon(Ljava/lang/String;)(feature.getId())
				};
				if(this.@pl.ismop.web.client.widgets.common.map.MapView::isHighlighted(Ljava/lang/String;)(featureId)) {
					icon['anchor']['x'] = 8
                    icon['anchor']['y'] = 8
				}

				this.@pl.ismop.web.client.widgets.common.map.MapView::layer.overrideStyle(feature, {
                    fillOpacity: 0.85,
                    strokeOpacity: 0.85,
                    strokeWeight: thisObject.@pl.ismop.web.client.widgets.common.map.MapView::getFeatureStrokeWidth(Ljava/lang/String;)(featureId) + 3,
					icon: icon
				});
                this.@pl.ismop.web.client.widgets.common.map.MapView::addSelected(Ljava/lang/String;)(featureId);
			} else {
                this.@pl.ismop.web.client.widgets.common.map.MapView::removeSelected(Ljava/lang/String;)(featureId);
                if(this.@pl.ismop.web.client.widgets.common.map.MapView::isHighlighted(Ljava/lang/String;)(featureId)) {
                    this.@pl.ismop.web.client.widgets.common.map.MapView::highlight(Ljava/lang/String;Z)(featureId, true);
                } else {
                    this.@pl.ismop.web.client.widgets.common.map.MapView::layer.revertStyle(feature);
                }
			}
		}
	}-*/;

	@Override
	public native void showPopup(String featureId, String contents) /*-{
		if(this.@pl.ismop.web.client.widgets.common.map.MapView::infoWindow != null) {
			this.@pl.ismop.web.client.widgets.common.map.MapView::infoWindow.close();
		}
		
		var feature = this.@pl.ismop.web.client.widgets.common.map.MapView::layer.getFeatureById(featureId);
		this.@pl.ismop.web.client.widgets.common.map.MapView::infoWindow = new $wnd.google.maps.InfoWindow({
			content: contents,
			position: feature.getGeometry().get(),
			pixelOffset: new $wnd.google.maps.Size(0,-7)
		});
		this.@pl.ismop.web.client.widgets.common.map.MapView::infoWindow.open(
				this.@pl.ismop.web.client.widgets.common.map.MapView::map);
		
	}-*/;

	@Override
	public native void hidePopup(String featureId) /*-{
		if(this.@pl.ismop.web.client.widgets.common.map.MapView::infoWindow != null) {
			this.@pl.ismop.web.client.widgets.common.map.MapView::infoWindow.close();
			this.@pl.ismop.web.client.widgets.common.map.MapView::infoWindow = null;
		}
	}-*/;

	private void featureHoverIn(String type, String id) {
		getPresenter().onFeatureHoverIn(type, id);
	}
	
	private void featureHoverOut(String type, String id) {
		getPresenter().onFeatureHoverOut(type, id);
	}
	
	private void featureClick(String type, String id) {
		getPresenter().onFeatureClick(type, id);
	}
	
	private int getFeatureStrokeWidth(String featureId) {
		if(featureId.startsWith("profile")) {
			return 8;
		} else {
			return 1;
		}
	}
	
	private String getFeatureFillColor(String featureId) {
		if(featureId.startsWith("profile")) {
			return "#41a7ec";
		} else if(featureId.startsWith("section")) {
			return "#afafaf";
		} else if(featureId.startsWith("deviceAggregate")) {
			return "#ebf56f";
		} else {
			return "#aaaaaa";
		}
	}
	
	private String getFeatureStrokeColor(String featureId) {
		if(featureId.startsWith("profile")) {
			return "#41a7ec";
		} else if(featureId.startsWith("section")) {
			return "#626262";
		} else if(featureId.startsWith("deviceAggregate")) {
			return "#ebf56f";
		} else {
			return "#aaaaaa";
		}
	}
	
	private String getFeatureIcon(String featureId) {
		if(featureId.startsWith("deviceAggregate")) {
			return "/icons/aggregate.png";
		} else {
			return "/icons/device-fiber.png";
		}
	}
	
	private String getSelectedFeatureIcon(String featureId) {
		if(featureId.startsWith("deviceAggregate")) {
			if(isHighlighted(featureId)) {
				return "/icons/aggregate-selected-highlighted.png";
			} else {
				return "/icons/aggregate-selected.png";
			}
		} else {
			if(isHighlighted(featureId)) {
				return "/icons/device-fiber-selected-highlighted.png";
			} else {
				return "/icons/device-fiber-selected.png";
			}
		}
	}

	private String getHighlightedFeatureIcon(String featureId) {
		if(featureId.startsWith("deviceAggregate")) {
			if(isSelected(featureId)) {
				return "/icons/aggregate-selected-highlighted.png";
			} else {
				return "/icons/aggregate-highlighted.png";
			}
		} else {
			if(isSelected(featureId)) {
				return "/icons/device-fiber-selected-highlighted.png";
			} else {
				return "/icons/device-fiber-highlighted.png";
			}
		}
	}
	
	private native void initMap() /*-{
		var map = new $wnd.google.maps.Map($doc.getElementById(this.@pl.ismop.web.client.widgets.common.map.MapView::elementId), {
			zoom: 8,
			scaleControl: true,
			draggable: false,
			zoomControl: false,
			streetViewControl: false,
			scrollwheel: false,
			disableDoubleClickZoom: true
		});
		this.@pl.ismop.web.client.widgets.common.map.MapView::map = map;
		
		var layerData = new $wnd.google.maps.Data();
		var thisObject = this;
		layerData.setStyle(function(feature) {
			var icon = {
				anchor: {
					x: 6,
					y: 6
				},
				url: thisObject.@pl.ismop.web.client.widgets.common.map.MapView::getFeatureIcon(Ljava/lang/String;)(feature.getId())
			};
		
			return {
				strokeColor: thisObject.@pl.ismop.web.client.widgets.common.map.MapView::getFeatureStrokeColor(Ljava/lang/String;)(feature.getId()),
				fillOpacity: 0.5,
				strokeOpacity: 0.7,
				fillColor: thisObject.@pl.ismop.web.client.widgets.common.map.MapView::getFeatureFillColor(Ljava/lang/String;)(feature.getId()),
				strokeWeight: thisObject.@pl.ismop.web.client.widgets.common.map.MapView::getFeatureStrokeWidth(Ljava/lang/String;)(feature.getId()),
				icon: icon
			};
		});
		
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer = layerData;
		layerData.setMap(map);
	}-*/;

	private native void addHoverHandlers() /*-{
		var thisObject = this;
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer.addListener('mouseover', function(event) {
			thisObject.@pl.ismop.web.client.widgets.common.map.MapView::featureHoverIn(Ljava/lang/String;Ljava/lang/String;)
					(event.feature.getProperty('type'), event.feature.getProperty('id'));
		});
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer.addListener('mouseout', function(event) {
			thisObject.@pl.ismop.web.client.widgets.common.map.MapView::featureHoverOut(Ljava/lang/String;Ljava/lang/String;)
					(event.feature.getProperty('type'), event.feature.getProperty('id'));
		});
	}-*/;
	
	private native void addClickListeners() /*-{
		var thisObject = this;
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer.addListener('click', function(event) {
			thisObject.@pl.ismop.web.client.widgets.common.map.MapView::featureClick(Ljava/lang/String;Ljava/lang/String;)
					(event.feature.getProperty('type'), event.feature.getProperty('id'));
		});
	}-*/;

	private native JavaScriptObject createLatLngBounds() /*-{
		return new $wnd.google.maps.LatLngBounds();
	}-*/;
	
	private native void extendBounds(JavaScriptObject bounds, double lat, double lng) /*-{
		bounds.extend(new $wnd.google.maps.LatLng(lat, lng));
	}-*/;

	private native void applyBounds(JavaScriptObject bounds) /*-{
		this.@pl.ismop.web.client.widgets.common.map.MapView::map.fitBounds(bounds);
	}-*/;

	private native void addMiddleButton(Element element) /*-{
		var container = $doc.createElement('div');
		container.style.padding = '5px';
		container.appendChild(element);
		this.@pl.ismop.web.client.widgets.common.map.MapView::map.controls[$wnd.google.maps.ControlPosition.TOP_RIGHT].push(container);
	}-*/;
}