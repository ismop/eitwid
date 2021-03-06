package pl.ismop.web.client.dap.measurement;

import java.util.Date;

import org.fusesource.restygwt.client.Json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Measurement {
	public Measurement() {
	}

	public Measurement(String timelineId, Date timestamp, double value) {
		this.timelineId = timelineId;
		this.timestamp = timestamp;
		this.value = value;
	}

	private String id;

	private double value;

	private Date timestamp;

	@Json(name = "timeline_id")
	private String timelineId;

	public String getTimelineId() {
		return timelineId;
	}

	public void setTimelineId(String timelineId) {
		this.timelineId = timelineId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
