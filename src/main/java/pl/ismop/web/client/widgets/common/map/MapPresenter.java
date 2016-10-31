package pl.ismop.web.client.widgets.common.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.geojson.GeoJsonFeature;
import pl.ismop.web.client.geojson.GeoJsonFeatures;
import pl.ismop.web.client.geojson.GeoJsonFeaturesEncDec;
import pl.ismop.web.client.geojson.Geometry;
import pl.ismop.web.client.geojson.MapFeature;
import pl.ismop.web.client.widgets.common.map.IMapView.IMapPresenter;

@Presenter(view = MapView.class, multiple = true)
public class MapPresenter extends BasePresenter<IMapView, MainEventBus> implements IMapPresenter {

	private GeoJsonFeaturesEncDec geoJsonEncoderDecoder;

	private Map<String, MapFeature> mapFeatures;

	private boolean hoverListeners;

	private boolean clickListeners;

	private boolean zoomed;

	private Map<String, String> featureStrokeColor;

	@Inject
	public MapPresenter(GeoJsonFeaturesEncDec geoJsonEncoderDecoder) {
		this.geoJsonEncoderDecoder = geoJsonEncoderDecoder;
		mapFeatures = new HashMap<>();
		featureStrokeColor = new HashMap<>();
	}

	public void add(MapFeature mapFeature) {
		if (!mapFeatures.keySet().contains(mapFeature.getFeatureId())) {
			Geometry geometry = mapFeature.getFeatureGeometry();

			if (geometry != null) {
				mapFeatures.put(mapFeature.getFeatureId(), mapFeature);
				view.addGeoJson(geoJson(mapFeature, geometry));

				if (mapFeature.isAdjustBounds()) {
					view.adjustBounds(collectAllPoints());
				}
			}
		}
	}

	public void rm(MapFeature mapFeature) {
		if(mapFeatures.containsKey(mapFeature.getFeatureId())) {
			view.removeFeature(mapFeature.getFeatureId());
			mapFeatures.remove(mapFeature.getFeatureId());
			view.hidePopup(mapFeature.getFeatureId());
		}
	}

	public void highlight(MapFeature mapFeature) {
		view.highlight(mapFeature.getFeatureId(), true);
	}

	public void unhighlight(MapFeature mapFeature) {
		view.highlight(mapFeature.getFeatureId(), false);
	}

	public void select(MapFeature mapFeature) {
		if(!mapFeatures.containsKey(mapFeature.getFeatureId())) {
			add(mapFeature);
		}
		view.selectFeature(mapFeature.getFeatureId(), true);
	}

	public void unselect(MapFeature mapFeature) {
		view.selectFeature(mapFeature.getFeatureId(), false);
	}

	public void reset(boolean leaveSections) {
		List<MapFeature> features = new ArrayList<>(mapFeatures.values());

		for (MapFeature entry : features) {
			if (entry.getFeatureType().equals("section") && leaveSections) {
				unhighlight(entry);
			} else {
				rm(entry);
			}
		}
	}

	/**
	 * Has to be invoked before the map widget is added to DOM.
	 */
	public void addHoverListeners() {
		hoverListeners = true;
	}

	/**
	 * Has to be invoked before the map widget is added to DOM.
	 */
	public void addClickListeners() {
		clickListeners = true;
	}

	public void addAction(String id, String label) {
		view.addButton(id, label);
	}

	public void removeAction(String id) {
		view.removeButton(id);
	}

	@Override
	public void onFeatureHoverOut(String type, String featureId) {
		MapFeature mapFeature = mapFeatures.get(featureId);

		if (mapFeature != null) {
			switch (type) {
				case "profile":
					view.highlight(featureId, false);
					eventBus.showProfileMetadata((Profile) mapFeature, false);
					break;
				case "section":
					view.highlight(featureId, false);
					eventBus.showSectionMetadata((Section) mapFeature, false);
					break;
				case "device":
					eventBus.showDeviceMetadata((Device) mapFeature, false);
					view.hidePopup(featureId);
					break;
				case "deviceAggregate":
					eventBus.showDeviceAggregateMetadata((DeviceAggregate) mapFeature, false);
					view.hidePopup(featureId);
			}
		}
	}

	@Override
	public void onFeatureHoverIn(String type, String featureId) {
		MapFeature mapFeature = mapFeatures.get(featureId);

		if (mapFeature != null) {
			switch (type) {
				case "profile":
					view.highlight(featureId, true);
					eventBus.showProfileMetadata((Profile) mapFeature, true);

					break;
				case "section":
					view.highlight(featureId, true);
					eventBus.showSectionMetadata((Section) mapFeature, true);

					break;
				case "device":
					Device device = (Device) mapFeature;
					eventBus.showDeviceMetadata(device, true);
					view.showPopup(featureId, device.getCustomId());

					break;
				case "deviceAggregate":
					DeviceAggregate deviceAggregate = (DeviceAggregate) mapFeature;
					eventBus.showDeviceAggregateMetadata(deviceAggregate, true);
					view.showPopup(featureId, deviceAggregate.getCustomId());

					break;
			}
		}
	}

	@Override
	public boolean isHoverListeners() {
		return hoverListeners;
	}

	@Override
	public boolean isClickListeners() {
		return clickListeners;
	}

	@Override
	public void onFeatureClick(String type, String featureId) {
		MapFeature mapFeature = mapFeatures.get(featureId);
		if (mapFeature != null) {
			switch (type) {
				case "profile":
					eventBus.profileClicked((Profile) mapFeature);
					break;
				case "section":
					eventBus.sectionClicked((Section) mapFeature);
					break;
				case "device":
					eventBus.deviceClicked((Device) mapFeature);
					break;
				case "deviceAggregate":
					eventBus.deviceAggregateClicked((DeviceAggregate) mapFeature);
			}
		}
	}

	public void zoomOnSection(Section section) {
		zoomed = true;

		if(!mapFeatures.keySet().contains(section.getFeatureId())) {
			add(section);
		}

		if(mapFeatures.keySet().contains(section.getFeatureId())) {
			view.adjustBounds(section.getShape().getCoordinates());
		}
	}

	public void zoomToAllSections() {
		view.adjustBounds(collectAllPoints());
	}

	@Override
	public void onZoomOut(String sectionId) {
		zoomed = false;
		eventBus.zoomOut(sectionId);
	}

	public void setLoadingState(boolean loading) {
		view.showLoadingPanel(loading);
	}

	public void setMoveable(boolean moveable) {
		getView().setMovable(moveable);
	}

	public void redrawMap() {
		view.redrawMap();
	}

	public boolean isZoomed() {
		return zoomed;
	}

	public void setFeatureStrokeColor(MapFeature feature, String color) {
		if (feature != null) {
			featureStrokeColor.put(feature.getFeatureId(), color);
		}
	}

	public void rmFeatureStrokeColor(MapFeature feature) {
		if (feature != null) {
			featureStrokeColor.remove(feature.getFeatureId());
		}
	}

	public void clearStrokeColors() {
		featureStrokeColor.clear();
	}

	@Override
	public String getFeatureStrokeColor(String featureId, String colourType) {
		if (featureStrokeColor.containsKey(featureId)) {
			return featureStrokeColor.get(featureId);
		} else if(featureId.startsWith("profile")) {
			return getProfileStrokeColor(colourType);
		} else if(featureId.startsWith("section")) {
			return getSectionStrokeColor(colourType);
		} else if(featureId.startsWith("deviceAggregate")) {
			return "#ebf56f";
		} else {
			return "#aaaaaa";
		}
	}

	private String getProfileStrokeColor(String colourType) {
		return "neosentio".equalsIgnoreCase(colourType) ? "#3880ff" : "#ff5538";
	}

	private String getSectionStrokeColor(String colourType) {
		switch (colourType) {
			case "A":
				return "#a6a6a6";
			case "B":
				return "#fff734";
			case "C":
				return "#878f39";
			case "D":
				return "#afbacc";
		}
		return "#ec8108";
	}

	private String geoJson(MapFeature mapFeature, Geometry geometry) {
		GeoJsonFeature feature = new GeoJsonFeature(mapFeature, geometry);

		return geoJsonEncoderDecoder.encode(new GeoJsonFeatures(feature)).toString();
	}

	private List<List<Double>> collectAllPoints() {
		List<List<Double>> allPoints = new ArrayList<>();
		for (MapFeature mapFeature : mapFeatures.values()) {
			if (mapFeature.getFeatureType().equals("section")) {
				allPoints.addAll(((Section) mapFeature).getShape().getCoordinates());
			} else if (mapFeature.getFeatureType().equals("profile")) {
				allPoints.addAll(((Profile) mapFeature).getShape().getCoordinates());
			}
		}

		return allPoints;
	}
}
