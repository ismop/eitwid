package pl.ismop.web.controllers.maps;

import java.util.List;

public class PointGeometry implements Geometry {
	private String type;
	private List<Double> coordinates;
	
	public PointGeometry() {
		setType("Point");
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Double> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<Double> coordinates) {
		this.coordinates = coordinates;
	}
}