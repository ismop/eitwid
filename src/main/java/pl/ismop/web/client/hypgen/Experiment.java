package pl.ismop.web.client.hypgen;

import java.util.Date;
import java.util.List;

import org.fusesource.restygwt.client.Json;

public class Experiment {
	private String id;
	private String name;
	private String status;
	
	@Json(name = "result_ids") private List<String> resultIds;
	@Json(name = "profile_ids") private List<String> profileIds;
	@Json(name = "start") private Date startDate;
	@Json(name = "end") private Date endDate;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<String> getResultIds() {
		return resultIds;
	}
	public void setResultIds(List<String> resultIds) {
		this.resultIds = resultIds;
	}
	public List<String> getProfileIds() {
		return profileIds;
	}
	public void setProfileIds(List<String> profileIds) {
		this.profileIds = profileIds;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@Override
	public String toString() {
		return "Experiment [id=" + id + ", name=" + name + ", status=" + status + ", resultIds=" + resultIds + ", profileIds=" + profileIds + ", startDate="
				+ startDate + ", endDate=" + endDate + "]";
	}
}