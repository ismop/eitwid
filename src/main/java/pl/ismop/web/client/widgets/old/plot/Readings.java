package pl.ismop.web.client.widgets.old.plot;

import java.util.Map;

public class Readings {
	private String label;
	private String unit;
	private Map<String, Number[][]> measurements;
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Map<String, Number[][]> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(Map<String, Number[][]> measurements) {
		this.measurements = measurements;
	}

	@Override
	public String toString() {
		return "Readings [label=" + label + ", unit=" + unit + ", measurements=" + measurements + "]";
	}
}