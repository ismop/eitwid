package pl.ismop.web.client.widgets.common.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.levee.PolygonShape;
import pl.ismop.web.client.dap.profile.Profile;
import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.geojson.GeoJsonFeature;
import pl.ismop.web.client.geojson.GeoJsonFeatures;
import pl.ismop.web.client.geojson.GeoJsonFeaturesEncDec;
import pl.ismop.web.client.geojson.LineGeometry;
import pl.ismop.web.client.widgets.common.map.IMapView.IMapPresenter;

@Presenter(view = MapView.class, multiple = true)
public class MapPresenter extends BasePresenter<IMapView, MainEventBus> implements IMapPresenter {
	private GeoJsonFeaturesEncDec geoJsonEncoderDecoder;
	private Map<String, Section> sections;
	private Map<String, Profile> profiles;
	
	@Inject
	public MapPresenter(GeoJsonFeaturesEncDec geoJsonEncoderDecoder) {
		this.geoJsonEncoderDecoder = geoJsonEncoderDecoder;
		sections = new HashMap<>();
		profiles = new HashMap<>();
	}
	
	public void initializeMap() {
		view.initMap();
	}
	
	public void addSection(Section section) {
		if(!sections.keySet().contains(section.getId())) {
			sections.put(section.getId(), section);
			
			if(section.getShape() != null) {
				view.addGeoJson(geoJsonEncoderDecoder.encode(sectionToGeoJsonFeatures(section)).toString());
				view.adjustBounds(collectPoints());
			}
		}
	}

	public void addProfile(Profile profile) {
		if(!profiles.keySet().contains(profile.getId())) {
			profiles.put(profile.getId(), profile);
			
			if(profile.getShape() != null) {
				view.addGeoJson(geoJsonEncoderDecoder.encode(profileToGeoJsonFeatures(profile)).toString());
				view.adjustBounds(collectPoints());
			}
		}
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

	private GeoJsonFeatures sectionToGeoJsonFeatures(Section section) {
		PolygonShape shape = section.getShape();
		LineGeometry lineGeometry = new LineGeometry();
		lineGeometry.setCoordinates(shape.getCoordinates());
		
		GeoJsonFeature feature = new GeoJsonFeature();
		feature.setGeometry(lineGeometry);
		feature.setId("section" + section.getId());
		feature.setProperties(new HashMap<String, String>());
		feature.getProperties().put("id", section.getId());
		feature.getProperties().put("name", feature.getId());
		feature.getProperties().put("type", "section");
		
		List<GeoJsonFeature> features = new ArrayList<>();
		features.add(feature);
		
		return new GeoJsonFeatures(features);
	}

	private GeoJsonFeatures profileToGeoJsonFeatures(Profile profile) {
		PolygonShape shape = profile.getShape();
		LineGeometry lineGeometry = new LineGeometry();
		lineGeometry.setCoordinates(shape.getCoordinates());
		
		GeoJsonFeature feature = new GeoJsonFeature();
		feature.setGeometry(lineGeometry);
		feature.setId("profile" + profile.getId());
		feature.setProperties(new HashMap<String, String>());
		feature.getProperties().put("id", profile.getId());
		feature.getProperties().put("name", feature.getId());
		feature.getProperties().put("type", "profile");
		
		List<GeoJsonFeature> features = new ArrayList<>();
		features.add(feature);
		
		return new GeoJsonFeatures(features);
	}

	private List<List<Double>> collectPoints() {
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