package pl.ismop.web.client.dap.threatlevel;

import java.util.List;

import org.fusesource.restygwt.client.Json;

public class ThreatLevelResponse {
	@Json(name = "threat_levels")
	private List<ThreatLevel> threatLevels;

	public List<ThreatLevel> getThreatLevels() {
		return threatLevels;
	}

	public void setThreatLevels(List<ThreatLevel> threatLevels) {
		this.threatLevels = threatLevels;
	}
}
