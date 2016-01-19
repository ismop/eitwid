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
	private Map<String, Section> sections;
	private Map<String, Profile> profiles;
	private Map<String, Device> devices;
	private Map<String, DeviceAggregate> deviceAggregates;
	private boolean hoverListeners;
	private boolean clickListeners;
	
	@Inject
	public MapPresenter(GeoJsonFeaturesEncDec geoJsonEncoderDecoder) {
		this.geoJsonEncoderDecoder = geoJsonEncoderDecoder;
		sections = new HashMap<>();
		profiles = new HashMap<>();
		devices = new HashMap<>();
		deviceAggregates = new HashMap<>();
	}
	
	public void add(Section section) {
		if(!sections.keySet().contains(section.getId())) {
			sections.put(section.getId(), section);
			addMapFeature(section, true);
		}
	}

	public void add(Profile profile) {
		if(!profiles.keySet().contains(profile.getId())) {
			profiles.put(profile.getId(), profile);
			addMapFeature(profile, true);
		}
	}

	public void add(Device device) {
		if(!devices.keySet().contains(device.getId())) {
			devices.put(device.getId(), device);
			addMapFeature(device, false);
		}
	}

	public void add(DeviceAggregate deviceAggregate) {
		if(!deviceAggregates.keySet().contains(deviceAggregate.getId())) {
			deviceAggregates.put(deviceAggregate.getId(), deviceAggregate);
			addMapFeature(deviceAggregate, false);
		}
	}

	private void addMapFeature(MapFeature mapFeature, boolean adjustBounds) {
		Geometry geometry = mapFeature.getFeatureGeometry();
		if(geometry != null) {
			view.addGeoJson(geoJson(mapFeature, geometry));
			if(adjustBounds) {
				view.adjustBounds(collectAllPoints());
			}
		}
	}

	private String geoJson(MapFeature mapFeature, Geometry geometry) {
		GeoJsonFeature feature = new GeoJsonFeature(mapFeature, geometry);
		return geoJsonEncoderDecoder.encode(new GeoJsonFeatures(feature)).toString();
	}

	public void rm(Device device) {
		rm(device, devices);
	}

	public void rm(Section section) {
		rm(section, sections);
	}

	public void rm(Profile profile) {
		rm(profile, profiles);
	}

	public void rm(DeviceAggregate deviceAggregate) {
		rm(deviceAggregate, deviceAggregates);
	}

	private void rm(MapFeature mapFeature, Map<String, ? extends MapFeature> collection) {
		if(collection.containsKey(mapFeature.getId())) {
			view.removeFeature(mapFeature.getFeatureId());
			collection.remove(mapFeature.getId());
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
		if(!devices.containsKey(device.getId())) {
			add(device);
		}

		view.selectFeature(device.getFeatureId(), true);
	}

	public void select(DeviceAggregate deviceAggregate) {
		if(!deviceAggregates.containsKey(deviceAggregate.getId())) {
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
		if(!leaveSections) {
			for(String sectionId : new ArrayList<>(sections.keySet())) {
				rm(sections.get(sectionId));
			}
		}

		for(String sectionId : new ArrayList<>(sections.keySet())) {
			highlight(sections.get(sectionId), false);
		}

		for(String profileId : new ArrayList<>(profiles.keySet())) {
			rm(profiles.get(profileId));
		}

		for(String deviceId : new ArrayList<>(devices.keySet())) {
			rm(devices.get(deviceId));
		}

		for(String deviceAggregateId : new ArrayList<>(deviceAggregates.keySet())) {
			rm(deviceAggregates.get(deviceAggregateId));
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
	public void onFeatureHoverOut(String type, String id) {
		switch(type) {
		case "profile":
			Profile profile = profiles.get(id);
			if(profile != null) {
				view.highlight(profile.getFeatureId(), false);
				eventBus.showProfileMetadata(profile, false);
			}
			
			break;
		case "section":
			Section section = sections.get(id);
			if(section != null) {
				view.highlight(section.getFeatureId(), false);
				eventBus.showSectionMetadata(sections.get(id), false);
			}
			
			break;
		case "device":
			Device device = devices.get(id);
			if(device != null) {
				eventBus.showDeviceMetadata(device, false);
				view.hidePopup(device.getFeatureId());
			}
			
			break;
		case "deviceAggregate":
			DeviceAggregate deviceAggregate = deviceAggregates.get(id);
			if(deviceAggregate != null) {
				eventBus.showDeviceAggregateMetadata(deviceAggregate, false);
				view.hidePopup(deviceAggregate.getFeatureId());
			}
		}
	}

	@Override
	public void onFeatureHoverIn(String type, String id) {
		switch(type) {
		case "profile":
			Profile profile = profiles.get(id);
			if(profile != null) {
				view.highlight(profile.getFeatureId(), true);
				eventBus.showProfileMetadata(profile, true);
			}
			
			break;
		case "section":
			Section section = sections.get(id);
			if(section != null) {
				view.highlight(section.getFeatureId(), true);
				eventBus.showSectionMetadata(section, true);
			}
			
			break;
		case "device":
			Device device = devices.get(id);
			if(device != null) {
				eventBus.showDeviceMetadata(device, true);
				view.showPopup(device.getFeatureId(), device.getCustomId());
			}
			
			break;
		case "deviceAggregate":
			DeviceAggregate deviceAggregate = deviceAggregates.get(id);
			if(deviceAggregate != null) {
				eventBus.showDeviceAggregateMetadata(deviceAggregate, true);
				view.showPopup(deviceAggregate.getFeatureId(), deviceAggregate.getCustomId());
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
	public void onFeatureClick(String type, String id) {
		switch(type) {
			case "profile":
				if(profiles.get(id) != null) {
					eventBus.profileClicked(profiles.get(id));
				}
			break;
			case "section":
				if(sections.get(id) != null) {
					eventBus.sectionClicked(sections.get(id));
				}
			break;
			case "device":
				if(devices.get(id) != null) {
					eventBus.deviceClicked(devices.get(id));
				}
			break;
			case "deviceAggregate":
				if(deviceAggregates.get(id) != null) {
					eventBus.deviceAggregateClicked(deviceAggregates.get(id));
				}
		}
	}

	public void zoomOnSection(Section section) {
		if(!sections.keySet().contains(section.getId())) {
			add(section);
		}
		
		if(sections.keySet().contains(section.getId())) {
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
		List<List<Double>> allPoints = new ArrayList<List<Double>>();

		for(Section section : sections.values()) {
			if(section.getShape() != null) {
				allPoints.addAll(section.getShape().getCoordinates());
			}
		}
		
		for(Profile profile : profiles.values()) {
			if(profile.getShape() != null) {
				allPoints.addAll(profile.getShape().getCoordinates());
			}
		}
		
		return allPoints;
	}
}