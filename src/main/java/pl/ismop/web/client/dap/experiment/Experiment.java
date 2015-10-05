package pl.ismop.web.client.dap.experiment;

import org.fusesource.restygwt.client.Json;

import java.util.Date;
import java.util.List;

public class Experiment {
    private String id;

    private String name;

    private String description;

    @Json(name = "start_date")
    private Date startDate;

    @Json(name = "end_date")
    private Date endtDate;

    @Json(name = "levee_id")
    private Integer leveeId;

    @Json(name = "timeline_ids")
    private List<Integer> timelineIds;

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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndtDate() {
        return endtDate;
    }

    public void setEndtDate(Date endtDate) {
        this.endtDate = endtDate;
    }

    public Integer getLeveeId() {
        return leveeId;
    }

    public void setLeveeId(Integer leveeId) {
        this.leveeId = leveeId;
    }

    public List<Integer> getTimelineIds() {
        return timelineIds;
    }

    public void setTimelineIds(List<Integer> timelineIds) {
        this.timelineIds = timelineIds;
    }
}
