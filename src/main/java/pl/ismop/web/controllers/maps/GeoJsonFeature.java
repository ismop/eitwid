package pl.ismop.web.controllers.maps;


public class GeoJsonFeature {
	private String type;
	private String id;
	private Geometry geometry;
	private String properties;
	
	public GeoJsonFeature() {
		setType("Feature");
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProperties() {
		return properties;
	}
	public void setProperties(String properties) {
		this.properties = properties;
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
}