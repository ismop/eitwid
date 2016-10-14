package pl.ismop.web.client.dap.threatlevel;

import java.util.Date;

import org.fusesource.restygwt.client.Json;

public class ThreatLevelAssessmentRun {

	@Json(name = "start_date")
	private Date startDate;

	private String status;

	private String explanation;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}
}
