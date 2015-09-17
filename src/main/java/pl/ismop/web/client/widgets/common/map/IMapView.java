package pl.ismop.web.client.widgets.common.map;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface IMapView extends IsWidget {
	interface IMapPresenter {
		void onFeatureHoverOut(String type, String id);

		void onFeatureHoverIn(String type, String id);

		boolean isHoverListeners();

		boolean isClickListeners();

		void onFeatureClick(String type, String id);

		void onZoomOut(String sectionId);
	}

	void adjustBounds(List<List<Double>> points);

	void addGeoJson(String geoJsonValue);

	void initMap();

	void removeFeature(String featureId);

	void highlight(String featureId, boolean highlight);

	void addButton(String id, String label);

	void removeButton(String id);

	void selectFeature(String featureId, boolean select);
}