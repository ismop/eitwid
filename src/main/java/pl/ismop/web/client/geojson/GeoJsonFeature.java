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

	public GeoJsonFeature(String id, String type, Geometry geometry) {
		this();

		this.geometry = geometry;
		this.id = type + "-" + id;
		this.properties = new HashMap<>();

		properties.put("id", id);
		properties.put("name", id);
		properties.put("type", type);
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