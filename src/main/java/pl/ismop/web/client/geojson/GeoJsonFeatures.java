package pl.ismop.web.client.geojson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeoJsonFeatures {
	private String type;
	private List<GeoJsonFeature> features;
	private Crs crs;

	public GeoJsonFeatures() {
		setType("FeatureCollection");
	}
	
	public GeoJsonFeatures(List<GeoJsonFeature> shapes) {
		this();
		setFeatures(shapes);
	}

	public GeoJsonFeatures(GeoJsonFeature... shapes) {
		this(Arrays.asList(shapes));
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<GeoJsonFeature> getFeatures() {
		return features;
	}
	public void setFeatures(List<GeoJsonFeature> features) {
		this.features = features;
	}

	public Crs getCrs() {
		return crs;
	}

	public void setCrs(Crs crs) {
		this.crs = crs;
	}
}