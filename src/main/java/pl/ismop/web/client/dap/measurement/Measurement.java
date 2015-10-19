package pl.ismop.web.client.dap.measurement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.fusesource.restygwt.client.Json;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Measurement {
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