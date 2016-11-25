package pl.ismop.web.client.dap.sensor;

import javax.xml.bind.annotation.XmlElement;

import org.fusesource.restygwt.client.Json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sensor {
	private String id;
	@Json(name = "custom_id")
	@XmlElement(name = "custom_id")
	private String customId;
	private Point placement;
	@Json(name = "measurement_type_unit")
	@XmlElement(name = "measurement_type_unit")
	private String unit;
	@Json(name = "measurement_type_name")
	@XmlElement(name = "measurement_type_name")
	private String unitLabel;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCustomId() {
		return customId;
	}
	public void setCustomId(String customId) {
		this.customId = customId;
	}
	public Point getPlacement() {
		return placement;
	}
	public void setPlacement(Point placement) {
		this.placement = placement;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getUnitLabel() {
		return unitLabel;
	}
	public void setUnitLabel(String unitLabel) {
		this.unitLabel = unitLabel;
	}
}
