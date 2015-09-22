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
import pl.ismop.web.client.dap.deviceaggregation.DeviceAggregation;
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
	private Map<String, DeviceAggregation> deviceAggregations;
	private boolean hoverListeners;
	private boolean clickListeners;
	
	@Inject
	public MapPresenter(GeoJsonFeaturesEncDec geoJsonEncoderDecoder) {
		this.geoJsonEncoderDecoder = geoJsonEncoderDecoder;
		sections = new HashMap<>();
		profiles = new HashMap<>();
		devices = new HashMap<>();
		deviceAggregations = new HashMap<>();
	}
	
	public void initializeMap() {
		view.initMap();
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

	public void reset() {
		// TODO Auto-generated method stub
		
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
	
	public void addDeviceAggregation(DeviceAggregation deviceAggregation) {
		if(deviceAggregation.getShape() != null && !deviceAggregations.keySet().contains(deviceAggregation.getId())) {
			deviceAggregations.put(deviceAggregation.getId(), deviceAggregation);
			
			Geometry shape = deviceAggregation.getShape();
			
			if(shape != null) {
				GeoJsonFeature feature = new GeoJsonFeature();
				feature.setGeometry(shape);
				feature.setId("deviceAggregation-" + deviceAggregation.getId());
				feature.setProperties(new HashMap<String, String>());
				feature.getProperties().put("id", deviceAggregation.getId());
				feature.getProperties().put("name", feature.getId());
				feature.getProperties().put("type", "deviceAggregation");
				
				List<GeoJsonFeature> features = new ArrayList<>();
				features.add(feature);
				view.addGeoJson(geoJsonEncoderDecoder.encode(new GeoJsonFeatures(features)).toString());
			}
		}
	}
	
	public void removeDeviceAggregation(DeviceAggregation deviceAggregation) {
		if(deviceAggregations.keySet().contains(deviceAggregation.getId())) {
			view.removeFeature("deviceAggregation-" + deviceAggregation.getId());
			deviceAggregations.remove(deviceAggregation).getId();
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
					eventBus.showProfileMetadata(profiles.get(id), false);
				}
			break;
			case "section":
				if(sections.get(id) != null) {
					eventBus.showSectionMetadata(sections.get(id), false);
				}
			break;
			case "device":
				if(devices.get(id) != null) {
					eventBus.showDeviceMetadata(devices.get(id), false);
				}
		}
	}

	@Override
	public void onFeatureHoverIn(String type, String id) {
		switch(type) {
			case "profile":
				if(profiles.get(id) != null) {
					eventBus.showProfileMetadata(profiles.get(id), true);
				}
			break;
			case "section":
				if(sections.get(id) != null) {
					eventBus.showSectionMetadata(sections.get(id), true);
				}
			break;
			case "device":
				if(devices.get(id) != null) {
					eventBus.showDeviceMetadata(devices.get(id), true);
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
				if(deviceAggregations.get(id) != null) {
					eventBus.deviceAggregateClicked(deviceAggregations.get(id));
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
		if(!devices.keySet().contains(device.getId())) {
			addDevice(device);
		}
		
		String featureId = "device-" + device.getId();
		view.selectFeature(featureId, select);
	}

	@Override
	public void onZoomOut(String sectionId) {
		eventBus.zoomOut(sectionId);
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