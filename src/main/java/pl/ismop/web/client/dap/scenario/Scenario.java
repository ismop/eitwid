package pl.ismop.web.client.dap.scenario;

import org.fusesource.restygwt.client.Json;

import java.util.List;

public class Scenario {
    private String id;

    private String name;

    private String description;

    @Json(name = "experiment_ids")
    private List<String> experiementIds;

    @Json(name = "timeline_ids")
    private List<String> timelineIds;
    
    @Json(name = "threat_level")
    private int threatLevel;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getExperiementIds() {
        return experiementIds;
    }

    public void setExperiementIds(List<String> experiementIds) {
        this.experiementIds = experiementIds;
    }

    public List<String> getTimelineIds() {
        return timelineIds;
    }

    public void setTimelineIds(List<String> timelineIds) {
        this.timelineIds = timelineIds;
    }
    
    public int getThreatLevel() {
		return threatLevel;
	}

	public void setThreatLevel(int threatLevel) {
		this.threatLevel = threatLevel;
	}
}
