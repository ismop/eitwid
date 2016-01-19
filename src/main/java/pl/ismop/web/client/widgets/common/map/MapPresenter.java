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

	public void highlight(Section section) {
		highlightSection(section, true);
	}

	public void unhighlight(Section section) {
		highlightSection(section, false);
	}

	private void highlightSection(Section section, boolean highlight) {
		view.highlight("section-" + section.getId(), highlight);
	}

	public void reset(boolean leaveSections) {
		if(!leaveSections) {
			for(String sectionId : new ArrayList<>(sections.keySet())) {
				rm(sections.get(sectionId));
			}
		}
		
		for(String sectionId : new ArrayList<>(sections.keySet())) {
			highlightSection(sections.get(sectionId), false);
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

	public void select(Device device) {
		select(device, true);
	}

	public void select(DeviceAggregate deviceAggregate) {
		select(deviceAggregate, true);
	}

	public void unselect(Device device) {
		select(device, false);
	}

	public void unselect(DeviceAggregate deviceAggregate) {
		select(deviceAggregate, false);
	}

	private void select(Device device, boolean select) {
		if(!devices.containsKey(device.getId())) {
			add(device);
		}

		String featureId = "device-" + device.getId();
		view.selectFeature(featureId, select);
	}

	private void select(DeviceAggregate deviceAggregate, boolean select) {
		if(!deviceAggregates.containsKey(deviceAggregate.getId())) {
			add(deviceAggregate);
		}

		String featureId = "deviceAggregate-" + deviceAggregate.getId();
		view.selectFeature(featureId, select);
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
			if(profiles.get(id) != null) {
				view.highlight("profile-" + id, false);
				eventBus.showProfileMetadata(profiles.get(id), false);
			}
			
			break;
		case "section":
			if(sections.get(id) != null) {
				view.highlight("section-" + id, false);
				eventBus.showSectionMetadata(sections.get(id), false);
			}
			
			break;
		case "device":
			if(devices.get(id) != null) {
				eventBus.showDeviceMetadata(devices.get(id), false);
				view.hidePopup("device-" + id);
			}
			
			break;
		case "deviceAggregate":
			if(deviceAggregates.get(id) != null) {
				eventBus.showDeviceAggregateMetadata(deviceAggregates.get(id), false);
				view.hidePopup("deviceAggregate-" + id);
			}
		}
	}

	@Override
	public void onFeatureHoverIn(String type, String id) {
		switch(type) {
		case "profile":
			if(profiles.get(id) != null) {
				view.highlight("profile-" + id, true);
				eventBus.showProfileMetadata(profiles.get(id), true);
			}
			
			break;
		case "section":
			if(sections.get(id) != null) {
				view.highlight("section-" + id, true);
				eventBus.showSectionMetadata(sections.get(id), true);
			}
			
			break;
		case "device":
			if(devices.get(id) != null) {
				eventBus.showDeviceMetadata(devices.get(id), true);
				view.showPopup("device-" + id, devices.get(id).getCustomId());
			}
			
			break;
		case "deviceAggregate":
			if(deviceAggregates.get(id) != null) {
				eventBus.showDeviceAggregateMetadata(deviceAggregates.get(id), true);
				view.showPopup("deviceAggregate-" + id, deviceAggregates.get(id).getCustomId());
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