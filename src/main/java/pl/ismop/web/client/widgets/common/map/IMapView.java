package pl.ismop.web.client.widgets.common.map;

import com.google.gwt.user.client.ui.IsWidget;

public interface IMapView extends IsWidget {
	interface IMapPresenter {
		
	}

	void adjustBounds();

	void addGeoJson(String geoJsonValue);

	void initMap();
}