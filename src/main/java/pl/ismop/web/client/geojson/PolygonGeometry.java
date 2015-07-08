package pl.ismop.web.client.geojson;

import java.util.List;

public class PolygonGeometry extends Geometry {
	private List<List<List<Double>>> coordinates;
	
	public PolygonGeometry() {
		super("Polygon");
	}

	public List<List<List<Double>>> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<List<List<Double>>> coordinates) {
		this.coordinates = coordinates;
	}
}