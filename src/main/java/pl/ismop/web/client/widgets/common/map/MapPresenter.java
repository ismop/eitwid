package pl.ismop.web.client.widgets.common.map;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;
import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregate;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.geojson.*;
import pl.ismop.web.client.widgets.common.map.IMapView.IMapPresenter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Presenter(view = MapView.class, multiple = true)
public class MapPresenter extends BasePresenter<IMapView, MainEventBus> implements IMapPresenter {
	private GeoJsonFeaturesEncDec geoJsonEncoderDecoder;
	private Map<String, MapFeature> mapFeatures;
	private boolean hoverListeners;
	private boolean clickListeners;
	
	@Inject
	public MapPresenter(GeoJsonFeaturesEncDec geoJsonEncoderDecoder) {
		this.geoJsonEncoderDecoder = geoJsonEncoderDecoder;
		mapFeatures = new HashMap<>();
	}
	
	public void add(Section mapFeature) {
		addMapFeature(mapFeature, mapFeature.isAdjustBounds());
	}

	public void add(Profile mapFeature) {
		addMapFeature(mapFeature, mapFeature.isAdjustBounds());
	}

	public void add(Device mapFeature) {
		addMapFeature(mapFeature, mapFeature.isAdjustBounds());
	}

	public void add(MapFeature mapFeature) {
		addMapFeature(mapFeature, mapFeature.isAdjustBounds());
	}

	private void addMapFeature(MapFeature mapFeature, boolean adjustBounds) {
		if(!mapFeatures.keySet().contains(mapFeature.getFeatureId())) {
			Geometry geometry = mapFeature.getFeatureGeometry();
			if (geometry != null) {
				mapFeatures.put(mapFeature.getFeatureId(), mapFeature);
				view.addGeoJson(geoJson(mapFeature, geometry));
				if (adjustBounds) {
					view.adjustBounds(collectAllPoints());
				}
			}
		}
	}

	private String geoJson(MapFeature mapFeature, Geometry geometry) {
		GeoJsonFeature feature = new GeoJsonFeature(mapFeature, geometry);
		return geoJsonEncoderDecoder.encode(new GeoJsonFeatures(feature)).toString();
	}

	public void rm(MapFeature mapFeature) {
		if(mapFeatures.containsKey(mapFeature.getFeatureId())) {
			view.removeFeature(mapFeature.getFeatureId());
			mapFeatures.remove(mapFeature.getFeatureId());
		}
	}

	public void highlight(MapFeature mapFeature) {
		highlight(mapFeature, true);
	}

	public void unhighlight(MapFeature mapFeature) {
		highlight(mapFeature, false);
	}

	private void highlight(MapFeature mapFeature, boolean highlight) {
		view.highlight(mapFeature.getFeatureId(), highlight);
	}

	public void select(Device device) {
		if(!mapFeatures.containsKey(device.getFeatureId())) {
			add(device);
		}

		view.selectFeature(device.getFeatureId(), true);
	}

	public void select(DeviceAggregate deviceAggregate) {
		if(!mapFeatures.containsKey(deviceAggregate.getFeatureId())) {
			add(deviceAggregate);
		}

		view.selectFeature(deviceAggregate.getFeatureId(), true);
	}

	public void select(MapFeature mapFeature) {
		view.selectFeature(mapFeature.getFeatureId(), true);
	}

	public void unselect(MapFeature mapFeature) {
		view.selectFeature(mapFeature.getFeatureId(), false);
	}

	public void reset(boolean leaveSections) {
		for (MapFeature entry : mapFeatures.values()) {
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
		if(mapFeature != null) {
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
		if(mapFeature != null) {
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
		eventBus.zoomOut(sectionId);
	}

	public void setLoadingState(boolean loading) {
		view.showLoadingPanel(loading);
	}

	private List<List<Double>> collectAllPoints() {
		List<List<Double>> allPoints = new ArrayList<>();
		for (MapFeature mapFeature : mapFeatures.values()) {
			if(mapFeature.getFeatureType().equals("section")) {
				allPoints.addAll(((Section) mapFeature).getShape().getCoordinates());
			} else if (mapFeature.getFeatureType().equals("profile")) {
				allPoints.addAll(((Profile) mapFeature).getShape().getCoordinates());
			}
		}
		
		return allPoints;
	}
}