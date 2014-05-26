package pl.ismop.web.controllers.maps;

import java.util.List;

public class PolygonGeometry implements Geometry {
	private String type;
	private List<List<List<Double>>> coordinates;
	
	public PolygonGeometry() {
		setType("Polygon");
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<List<List<Double>>> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<List<List<Double>>> coordinates) {
		this.coordinates = coordinates;
	}
}