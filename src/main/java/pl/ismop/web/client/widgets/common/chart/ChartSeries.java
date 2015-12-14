package pl.ismop.web.client.widgets.common.chart;

public class ChartSeries {
	private String name;
	
	private String deviceId;
	
	private String parameterId;
	
	private String unit;
	
	private String label;
	
	private Number[][] values;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Number[][] getValues() {
		return values;
	}

	public void setValues(Number[][] values) {
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}