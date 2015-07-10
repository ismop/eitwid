package pl.ismop.web.client.dap.deviceaggregation;

import java.util.List;

public class PointShape {
	private String type;
	private List<Double> coordinates;
	
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
	@Override
	public String toString() {
		return "Shape [type=" + type + ", coordinates=" + coordinates + "]";
	}
}