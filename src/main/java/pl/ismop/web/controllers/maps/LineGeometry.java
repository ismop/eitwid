package pl.ismop.web.controllers.maps;

import java.util.List;

public class LineGeometry extends Geometry {
	private List<List<Double>> coordinates;

	public LineGeometry() {
		super("LineString");
	}

	public List<List<Double>> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<List<Double>> coordinates) {
		this.coordinates = coordinates;
	}
}