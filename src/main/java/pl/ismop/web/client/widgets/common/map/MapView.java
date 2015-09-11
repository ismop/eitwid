package pl.ismop.web.client.widgets.common.map;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
	
	private JavaScriptObject map, layer;

	@UiField
	FlowPanel panel;
	
	public MapView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		elementId = "map-" + hashCode();
		panel.getElement().setAttribute("id", elementId);
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
		initMap();
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
	public native void addGeoJson(String geoJsonValue) /*-{
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer.addGeoJson(JSON.parse(geoJsonValue));
	}-*/;

	@Override
	public native void initMap() /*-{
		var map = new $wnd.google.maps.Map($doc.getElementById(this.@pl.ismop.web.client.widgets.common.map.MapView::elementId), {
			zoom: 8,
			scaleControl: true
			
		});
		this.@pl.ismop.web.client.widgets.common.map.MapView::map = map;
		
		var layerData = new $wnd.google.maps.Data();
		layerData.setStyle(function(feature) {
			return {
				strokeColor: '#aaaaaa',
				fillOpacity: 0.9,
				strokeOpacity: 1.0,
				fillColor: '#aaaaaa',
				strokeWeight: 2
			};
		});
		
		var thisObject = this;
		layerData.addListener('mouseover', function(event) {
			thisObject.@pl.ismop.web.client.widgets.common.map.MapView::featureHoverIn(Ljava/lang/String;Ljava/lang/String;)
					(event.feature.getProperty('type'), event.feature.getProperty('id'));
		});
		layerData.addListener('mouseout', function(event) {
			thisObject.@pl.ismop.web.client.widgets.common.map.MapView::featureHoverOut(Ljava/lang/String;Ljava/lang/String;)
					(event.feature.getProperty('type'), event.feature.getProperty('id'));
		});
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer = layerData;
		layerData.setMap(map);
	}-*/;
	
	private void featureHoverIn(String type, String id) {
		getPresenter().onFeatureHoverIn(type, id);
	}
	
	private void featureHoverOut(String type, String id) {
		getPresenter().onFeatureHoverOut(type, id);
	}
	
	private native JavaScriptObject createLatLngBounds() /*-{
		return new $wnd.google.maps.LatLngBounds();
	}-*/;
	
	private native void extendBounds(JavaScriptObject bounds, Double lat, Double lng) /*-{
		bounds.extend(new $wnd.google.maps.LatLng(lat, lng));
	}-*/;

	private native void applyBounds(JavaScriptObject bounds) /*-{
		this.@pl.ismop.web.client.widgets.common.map.MapView::map.fitBounds(bounds);
	}-*/;
}