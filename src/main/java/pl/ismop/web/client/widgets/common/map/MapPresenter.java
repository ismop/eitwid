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
	
	public void add(Section section) {
		if(!sections.keySet().contains(section.getId())) {
			sections.put(section.getId(), section);
			PolygonShape shape = section.getShape();

			if(isValid(shape)) {
				List<List<List<Double>>> polygonCoordinates = new ArrayList<List<List<Double>>>();
				polygonCoordinates.add(shape.getCoordinates());
				PolygonGeometry polygonGeometry = new PolygonGeometry();
				polygonGeometry.setCoordinates(polygonCoordinates);

				view.addGeoJson(geoJson(section.getId(), section.getFeatureType(), polygonGeometry));
				view.adjustBounds(collectAllPoints());
			}
		}
	}

	public void add(Profile profile) {
		if(!profiles.keySet().contains(profile.getId())) {
			profiles.put(profile.getId(), profile);
			PolygonShape shape = profile.getShape();

			if(shape != null) {
				LineGeometry lineGeometry = new LineGeometry();
				lineGeometry.setCoordinates(shape.getCoordinates());

				view.addGeoJson(geoJson(profile.getId(), profile.getFeatureType(), lineGeometry));
				view.adjustBounds(collectAllPoints());
			}
		}
	}

	public void add(Device device) {
		if(device.getPlacement() != null && !devices.keySet().contains(device.getId())) {
			devices.put(device.getId(), device);
			PointShape shape = device.getPlacement();

			if(shape != null) {
				PointGeometry pointGeometry = new PointGeometry();
				pointGeometry.setCoordinates(shape.getCoordinates());

				view.addGeoJson(geoJson(device.getId(), device.getFeatureType(), pointGeometry));
			}
		}
	}

	public void add(DeviceAggregate deviceAggregate) {
		Geometry shape = deviceAggregate.getShape();
		if(shape != null && !deviceAggregates.keySet().contains(deviceAggregate.getId())) {
			deviceAggregates.put(deviceAggregate.getId(), deviceAggregate);
			view.addGeoJson(geoJson(deviceAggregate.getId(), deviceAggregate.getFeatureType(), shape));
		}
	}

	private String geoJson(String id, String type, Geometry geometry) {
		GeoJsonFeature feature = new GeoJsonFeature(id, type, geometry);
		return geoJsonEncoderDecoder.encode(new GeoJsonFeatures(feature)).toString();
	}

	private boolean isValid(PolygonShape shape) {
		if (shape != null) {
			List<List<Double>> coordinates = shape.getCoordinates();
			return String.valueOf(coordinates.get(0).get(0)).
					equals(String.valueOf(coordinates.get(coordinates.size() - 1).get(0)));
		} else {
			return false;
		}
	}

	public void rm(Device device) {
		if(devices.keySet().contains(device.getId())) {
			view.removeFeature(device.getFeatureId());
			devices.remove(device.getId());
		}
	}

	public void rm(Section section) {
		if(sections.keySet().contains(section.getId())) {
			view.removeFeature(section.getFeatureId());
			sections.remove(section.getId());
		}
	}

	public void rm(Profile profile) {
		if(profiles.keySet().contains(profile.getId())) {
			view.removeFeature(profile.getFeatureId());
			profiles.remove(profile.getId());
		}
	}

	public void rm(DeviceAggregate deviceAggregate) {
		if(deviceAggregates.containsKey(deviceAggregate.getId())) {
			view.removeFeature(deviceAggregate.getFeatureId());
			deviceAggregates.remove(deviceAggregate.getId());
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