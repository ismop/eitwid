package pl.ismop.web.client.dap.result;

import org.fusesource.restygwt.client.Json;

public class Result {
	private String id;
	private float similarity;
	
	@Json(name = "profile_id")
	private String profileId;
	
	@Json(name = "experiment_id")
	private String experimentId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getSimilarity() {
		return similarity;
	}
	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}
	public String getProfileId() {
		return profileId;
	}
	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}
	public String getExperimentId() {
		return experimentId;
	}
	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}
}