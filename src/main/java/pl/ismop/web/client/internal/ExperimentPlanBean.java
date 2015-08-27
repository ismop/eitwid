package pl.ismop.web.client.internal;

import java.util.Date;

public class ExperimentPlanBean {
	private Long id;
	
	private String name;
	
	private Date startDate;
	
	private String wave;
	
	private Float margin;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getWave() {
		return wave;
	}

	public void setWave(String wave) {
		this.wave = wave;
	}

	public Float getMargin() {
		return margin;
	}

	public void setMargin(Float margin) {
		this.margin = margin;
	}
}
