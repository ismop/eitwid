package pl.ismop.web.client.dap.experiment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gwt.i18n.client.DateTimeFormat;
import org.fusesource.restygwt.client.Json;
import pl.ismop.web.client.dap.section.Section;

import java.util.Date;
import java.util.List;

public class Experiment {
    private static final DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601);

    private String id;

    private String name;

    private String description;

    @Json(name = "start_date")
    private Date start;

    @Json(name = "end_date")
    private Date end;

    @Json(name = "levee_id")
    private Integer leveeId;

    @Json(name = "timeline_ids")
    private List<Integer> timelineIds;

    @JsonIgnore
    private List<Section> sections;

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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
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

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public List<Section> getSections() {
        return sections;
    }
}
