package pl.ismop.web.client.dap.levee;

import java.util.List;

public class Shape {
	private String type;
	private List<List<Double>> coordinates;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<List<Double>> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<List<Double>> coordinates) {
		this.coordinates = coordinates;
	}
	@Override
	public String toString() {
		return "Shape [type=" + type + ", coordinates=" + coordinates + "]";
	}
}