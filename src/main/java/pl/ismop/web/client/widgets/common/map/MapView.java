package pl.ismop.web.client.widgets.common.map;

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
	public void adjustBounds() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public native void addGeoJson(String geoJsonValue) /*-{
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer.addGeoJson(JSON.parse(geoJsonValue));
	}-*/;

	@Override
	public native void initMap() /*-{
		var map = new $wnd.google.maps.Map($doc.getElementById(this.@pl.ismop.web.client.widgets.common.map.MapView::elementId), {
			center: {lat: -34.397, lng: 150.644},
			zoom: 8,
			scaleControl: true
			
		});
		this.@pl.ismop.web.client.widgets.common.map.MapView::map = map;
		
		var layerData = new $wnd.google.maps.Data();
		this.@pl.ismop.web.client.widgets.common.map.MapView::layer = layerData;
		layerData.setMap(map);
	}-*/;
}