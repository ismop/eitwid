package pl.ismop.web.client.dap.threatlevel;

import org.fusesource.restygwt.client.Json;

public class Scenario {
	private Float similarity;

	private Long scenarioId;

	@Json(name = "threat_level")
	private int threatLevel;

	private int offset;

	private String name;

	private String description;

	public Float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(Float similarity) {
		this.similarity = similarity;
	}

	public Long getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(Long scenarioId) {
		this.scenarioId = scenarioId;
	}

	public int getThreatLevel() {
		return threatLevel;
	}

	public void setThreatLevel(int threatLevel) {
		this.threatLevel = threatLevel;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
