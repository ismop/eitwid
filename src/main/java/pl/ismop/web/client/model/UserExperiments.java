package pl.ismop.web.client.model;

import java.util.List;

public class UserExperiments {
	private String userLogin;
	private List<String> experimentIds;
	
	public String getUserLogin() {
		return userLogin;
	}
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}
	public List<String> getExperimentIds() {
		return experimentIds;
	}
	public void setExperimentIds(List<String> experimentIds) {
		this.experimentIds = experimentIds;
	}
}