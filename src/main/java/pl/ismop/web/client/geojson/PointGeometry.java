package pl.ismop.web.client.geojson;

import java.util.List;

public class PointGeometry extends Geometry {
	private List<Double> coordinates;
	
	public PointGeometry() {
		super("Point");
	}

	public List<Double> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Double> coordinates) {
		this.coordinates = coordinates;
	}
}