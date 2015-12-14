package pl.ismop.web.client.widgets.old.plot;

import java.util.Map;

public class Readings {
	private String parameterName;
	private String typeName;

	private String unit;
	private Map<String, Number[][]> measurements;
	private String parameterId;
	
	public String getParameterName() {
		return parameterName;
	}
	
	public void setParameterName(String label) {
		this.parameterName = label;
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
		return "Readings [label=" + parameterName + ", unit=" + unit + ", measurements=" + measurements + "]";
	}

	public void setParameterId(String id) {
		this.parameterId = id;
	}

	public String getParameterId() {
		return parameterId;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}