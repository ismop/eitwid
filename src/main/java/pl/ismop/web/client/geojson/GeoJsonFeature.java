package pl.ismop.web.client.geojson;

import java.util.HashMap;
import java.util.Map;

public class GeoJsonFeature {

	private String type;

	private String id;

	private Geometry geometry;

	private Map<String, String> properties;

	public GeoJsonFeature() {
		setType("Feature");
	}

	public GeoJsonFeature(MapFeature mapFeature, Geometry geometry) {
		this();

		this.geometry = geometry;
		this.id = mapFeature.getFeatureId();
		this.properties = new HashMap<>();

		properties.put("id", mapFeature.getId());
		properties.put("name", mapFeature.getName());
		properties.put("type", mapFeature.getFeatureType());
		properties.putAll(mapFeature.getAdditionalFeatureProperties());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
