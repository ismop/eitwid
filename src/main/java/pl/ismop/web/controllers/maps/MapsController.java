package pl.ismop.web.controllers.maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.ismop.web.client.dap.section.Section;
import pl.ismop.web.client.dap.sensor.Sensor;
import pl.ismop.web.services.DapService;

@RestController
@RequestMapping("/maps")
public class MapsController {
	private AtomicInteger idGenerator;
	
	@Autowired private DapService dapService;
	
	public MapsController() {
		idGenerator = new AtomicInteger();
	}
	
	@RequestMapping(value = "/geojson", produces = "application/json")
	public GeoJsonFeatures geojson() {
		List<GeoJsonFeature> shapes = dapService.getLevees()
				.stream()
				.map(levee -> {
					GeoJsonFeature geoJsonFeature = new GeoJsonFeature();
					geoJsonFeature.setId(String.valueOf(idGenerator.incrementAndGet()));
					geoJsonFeature.setProperties(new HashMap<>());
					geoJsonFeature.getProperties().put("id", levee.getId());
					geoJsonFeature.getProperties().put("name", levee.getName());
					geoJsonFeature.getProperties().put("type", "levee");
					
					PolygonGeometry geometry = new PolygonGeometry();
					ArrayList<List<List<Double>>> polygons = new ArrayList<List<List<Double>>>();
					polygons.add(reverseCoordinates(levee.getShape().getCoordinates()));
					
					//we need to close the polygon
					polygons.get(0).add(levee.getShape().getCoordinates().get(0));
					geometry.setCoordinates(polygons);
					geoJsonFeature.setGeometry(geometry);
					
					return geoJsonFeature;
				})
				.collect(Collectors.toList());
		
		GeoJsonFeatures result = new GeoJsonFeatures(shapes);
		
		return result;
	}
	
	@RequestMapping(value = "/sensors", produces = "application/json")
	public GeoJsonFeatures sensors() {
		List<GeoJsonFeature> shapes = dapService.getSensors()
				.stream()
				.map(sensor -> {
					GeoJsonFeature geoJsonFeature = new GeoJsonFeature();
					geoJsonFeature.setId(String.valueOf(idGenerator.incrementAndGet()));
					geoJsonFeature.setProperties(new HashMap<>());
					geoJsonFeature.getProperties().put("id", sensor.getId());
					geoJsonFeature.getProperties().put("name", sensor.getCustomId());
					geoJsonFeature.getProperties().put("type", "sensor");
					
					PointGeometry point = new PointGeometry();
					point.setCoordinates(sensor.getPlacement().getCoordinates());
					geoJsonFeature.setGeometry(point);
					
					return geoJsonFeature;
				})
				.collect(Collectors.toList());
		
		GeoJsonFeatures result = new GeoJsonFeatures(shapes);
		
		return result;
	}
	
	@RequestMapping(value = "/profiles", produces = "application/json")
	public GeoJsonFeatures profiles() {
		List<Section> profiles = dapService.getSections();
		List<GeoJsonFeature> shapes = profiles.stream()
				.map(profile -> {
					GeoJsonFeature geoJsonFeature = new GeoJsonFeature();
					geoJsonFeature.setId(String.valueOf(idGenerator.incrementAndGet()));
					geoJsonFeature.setProperties(new HashMap<>());
					geoJsonFeature.getProperties().put("id", profile.getId());
					geoJsonFeature.getProperties().put("name", profile.getName());
					geoJsonFeature.getProperties().put("type", "profile");
					
					PolygonGeometry geometry = new PolygonGeometry();
					ArrayList<List<List<Double>>> polygons = new ArrayList<List<List<Double>>>();
					polygons.add(profile.getShape().getCoordinates());
					
					//we need to close the polygon
					polygons.get(0).add(profile.getShape().getCoordinates().get(0));
					geometry.setCoordinates(polygons);
					geoJsonFeature.setGeometry(geometry);
					
					return geoJsonFeature;
				})
				.collect(Collectors.toList());
		
		GeoJsonFeatures result = new GeoJsonFeatures(shapes);
		
		return result;
	}

	private List<List<Double>> profileToLine(Section profile, Map<String, Sensor> sensors) {
		List<List<Double>> result = new ArrayList<>();
		
		for(String sensorId : profile.getSensorIds()) {
			result.add(Arrays.asList(new Double[] {
					sensors.get(sensorId).getPlacement().getCoordinates().get(1),
					sensors.get(sensorId).getPlacement().getCoordinates().get(0)
			}));
		}
		
		return result;
	}

	private List<Double> reversePointCoordinates(List<Double> coordinates) {
		return Arrays.asList(new Double[] {coordinates.get(1), coordinates.get(0), coordinates.get(2)});
	}

	private List<List<Double>> reverseCoordinates(List<List<Double>> coordinates) {
		for(List<Double> point : coordinates) {
			Double first = point.remove(0);
			Double second = point.remove(0);
			
			if(point.size() > 0) {
				point.remove(0);
			}
			
			point.add(second);
			point.add(first);
			//third coordinate breaks OpenLayers
//			point.add(third);
		}
		
		return coordinates;
	}
}