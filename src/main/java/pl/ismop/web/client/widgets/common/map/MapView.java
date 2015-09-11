package pl.ismop.web.client.widgets.common.map;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class MapView extends Composite implements IMapView {
	private static MapViewUiBinder uiBinder = GWT.create(MapViewUiBinder.class);

	interface MapViewUiBinder extends UiBinder<Widget, MapView> {}
	
	private String elementId;
	private JavaScriptObject map;
	private JavaScriptObject layer;

	@UiField
	FlowPanel panel;
	
	public MapView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		elementId = "map-" + hashCode();
		panel.getElement().setAttribute("id", elementId);
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
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer = layerData;
		layerData.setMap(map);
	}-*/;
	
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