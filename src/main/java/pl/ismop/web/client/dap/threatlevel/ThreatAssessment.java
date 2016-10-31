package pl.ismop.web.client.dap.threatlevel;

import java.util.Date;
import java.util.List;


public class ThreatAssessment {

	private Date date;

	private String status;

	private List<Scenario> scenarios;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}
}
