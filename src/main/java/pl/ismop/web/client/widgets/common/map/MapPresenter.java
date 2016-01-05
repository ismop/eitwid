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
import pl.ismop.web.client.dap.deviceaggregation.PointShape;
import pl.ismop.web.client.dap.levee.PolygonShape;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.geojson.GeoJsonFeature;
import pl.ismop.web.client.geojson.GeoJsonFeatures;
import pl.ismop.web.client.geojson.GeoJsonFeaturesEncDec;
import pl.ismop.web.client.geojson.Geometry;
import pl.ismop.web.client.geojson.LineGeometry;
import pl.ismop.web.client.geojson.PointGeometry;
import pl.ismop.web.client.geojson.PolygonGeometry;
import pl.ismop.web.client.widgets.common.map.IMapView.IMapPresenter;

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
	
	public void addSection(Section section) {
		if(!sections.keySet().contains(section.getId())) {
			sections.put(section.getId(), section);
			
			if(section.getShape() != null) {
				view.addGeoJson(geoJsonEncoderDecoder.encode(sectionToGeoJsonFeatures(section)).toString());
				view.adjustBounds(collectAllPoints());
			}
		}
	}
	
	public void highlightSection(Section section, boolean highlight) {
		view.highlight("section-" + section.getId(), highlight);
	}

	public void addProfile(Profile profile) {
		if(!profiles.keySet().contains(profile.getId())) {
			profiles.put(profile.getId(), profile);
			
			if(profile.getShape() != null) {
				view.addGeoJson(geoJsonEncoderDecoder.encode(profileToGeoJsonFeatures(profile)).toString());
				view.adjustBounds(collectAllPoints());
			}
		}
	}

	public void reset(boolean leaveSections) {
		if(!leaveSections) {
			for(String sectionId : new ArrayList<>(sections.keySet())) {
				removeSection(sections.get(sectionId));
			}
		}
		
		for(String sectionId : new ArrayList<>(sections.keySet())) {
			highlightSection(sections.get(sectionId), false);
		}
		
		for(String profileId : new ArrayList<>(profiles.keySet())) {
			removeProfile(profiles.get(profileId));
		}
		
		for(String deviceId : new ArrayList<>(devices.keySet())) {
			removeDevice(devices.get(deviceId));
		}
		
		for(String deviceAggregateId : new ArrayList<>(deviceAggregates.keySet())) {
			removeDeviceAggregate(deviceAggregates.get(deviceAggregateId));
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
	
	public void addDevice(Device device) {
		if(device.getPlacement() != null && !devices.keySet().contains(device.getId())) {
			devices.put(device.getId(), device);
			
			PointShape shape = device.getPlacement();
			
			if(shape != null) {
				PointGeometry pointGeometry = new PointGeometry();
				pointGeometry.setCoordinates(shape.getCoordinates());
				
				GeoJsonFeature feature = new GeoJsonFeature();
				feature.setGeometry(pointGeometry);
				feature.setId("device-" + device.getId());
				feature.setProperties(new HashMap<String, String>());
				feature.getProperties().put("id", device.getId());
				feature.getProperties().put("name", feature.getId());
				feature.getProperties().put("type", "device");
				
				List<GeoJsonFeature> features = new ArrayList<>();
				features.add(feature);
				view.addGeoJson(geoJsonEncoderDecoder.encode(new GeoJsonFeatures(features)).toString());
			}
		}
	}
	
	public void removeDevice(Device device) {
		if(devices.keySet().contains(device.getId())) {
			view.removeFeature("device-" + device.getId());
			devices.remove(device.getId());
		}
	}
	
	public void removeSection(Section section) {
		if(sections.keySet().contains(section.getId())) {
			view.removeFeature("section-" + section.getId());
			sections.remove(section.getId());
		}
	}
	
	public void removeProfile(Profile profile) {
		if(profiles.keySet().contains(profile.getId())) {
			view.removeFeature("profile-" + profile.getId());
			profiles.remove(profile.getId());
		}
	}
	
	public void addDeviceAggregate(DeviceAggregate deviceAggregate) {
		if(deviceAggregate.getShape() != null && !deviceAggregates.keySet().contains(deviceAggregate.getId())) {
			deviceAggregates.put(deviceAggregate.getId(), deviceAggregate);
			
			Geometry shape = deviceAggregate.getShape();
			
			if(shape != null) {
				GeoJsonFeature feature = new GeoJsonFeature();
				feature.setGeometry(shape);
				feature.setId("deviceAggregate-" + deviceAggregate.getId());
				feature.setProperties(new HashMap<String, String>());
				feature.getProperties().put("id", deviceAggregate.getId());
				feature.getProperties().put("name", feature.getId());
				feature.getProperties().put("type", "deviceAggregate");
				
				List<GeoJsonFeature> features = new ArrayList<>();
				features.add(feature);
				view.addGeoJson(geoJsonEncoderDecoder.encode(new GeoJsonFeatures(features)).toString());
			}
		}
	}
	
	public void removeDeviceAggregate(DeviceAggregate deviceAggregate) {
		if(deviceAggregates.containsKey(deviceAggregate.getId())) {
			view.removeFeature("deviceAggregate-" + deviceAggregate.getId());
			deviceAggregates.remove(deviceAggregate.getId());
		}
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
			addSection(section);
		}
		
		if(sections.keySet().contains(section.getId())) {
			view.adjustBounds(section.getShape().getCoordinates());
		}
	}

	public void zoomToAllSections() {
		view.adjustBounds(collectAllPoints());
	}

	public void selectDevice(Device device, boolean select) {
		if(!devices.containsKey(device.getId())) {
			addDevice(device);
		}
		
		String featureId = "device-" + device.getId();
		view.selectFeature(featureId, select);
	}

	public void selectDeviceAggregate(DeviceAggregate deviceAggregate, boolean select) {
		if(!deviceAggregates.containsKey(deviceAggregate.getId())) {
			addDeviceAggregate(deviceAggregate);
		}
		
		String featureId = "deviceAggregate-" + deviceAggregate.getId();
		view.selectFeature(featureId, select);
	}

	@Override
	public void onZoomOut(String sectionId) {
		eventBus.zoomOut(sectionId);
	}

	public void setLoadingState(boolean loading) {
		view.showLoadingPanel(loading);
	}

	private GeoJsonFeatures sectionToGeoJsonFeatures(Section section) {
		PolygonShape shape = section.getShape();
		List<List<List<Double>>> polygonCoordinates = new ArrayList<List<List<Double>>>();
		polygonCoordinates.add(shape.getCoordinates());
		PolygonGeometry polygonGeometry = new PolygonGeometry();
		polygonGeometry.setCoordinates(polygonCoordinates);
		
		GeoJsonFeature feature = new GeoJsonFeature();
		feature.setGeometry(polygonGeometry);
		feature.setId("section-" + section.getId());
		feature.setProperties(new HashMap<String, String>());
		feature.getProperties().put("id", section.getId());
		feature.getProperties().put("name", feature.getId());
		feature.getProperties().put("type", "section");
		
		List<GeoJsonFeature> features = new ArrayList<>();

		//checking if the multipoint shape is closed, if not it is not drawn so there are no map errors
		if(String.valueOf(polygonCoordinates.get(0).get(0).get(0)).equals(String.valueOf(polygonCoordinates.get(0).get(polygonCoordinates.get(0).size() - 1).get(0)))) {
			features.add(feature);
		}
		
		return new GeoJsonFeatures(features);
	}

	private GeoJsonFeatures profileToGeoJsonFeatures(Profile profile) {
		PolygonShape shape = profile.getShape();
		LineGeometry lineGeometry = new LineGeometry();
		lineGeometry.setCoordinates(shape.getCoordinates());
		
		GeoJsonFeature feature = new GeoJsonFeature();
		feature.setGeometry(lineGeometry);
		feature.setId("profile-" + profile.getId());
		feature.setProperties(new HashMap<String, String>());
		feature.getProperties().put("id", profile.getId());
		feature.getProperties().put("name", feature.getId());
		feature.getProperties().put("type", "profile");
		
		List<GeoJsonFeature> features = new ArrayList<>();
		features.add(feature);
		
		return new GeoJsonFeatures(features);
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