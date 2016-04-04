package pl.ismop.web.client.widgets.common.map;

import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

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

	void removeFeature(String featureId);

	void highlight(String featureId, boolean highlight);

	void addButton(String id, String label);

	void removeButton(String id);

	void selectFeature(String featureId, boolean select);

	void showLoadingPanel(boolean show);

	void setMovable(boolean moveable);

	void showPopup(String featureId, String contents);

	void hidePopup(String featureId);
}