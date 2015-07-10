package pl.ismop.web.client.dap.section;

import javax.xml.bind.annotation.XmlElement;

import org.fusesource.restygwt.client.Json;

import pl.ismop.web.client.dap.levee.PolygonShape;

public class Section {
	private String id;
	private String name;
	private PolygonShape shape;
	
	@Json(name = "levee_id")
	@XmlElement(name = "levee_id")
	private String leveeId;
	
	public String getLeveeId() {
		return leveeId;
	}
	
	public void setLeveeId(String leveeId) {
		this.leveeId = leveeId;
	}
	
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
	
	public PolygonShape getShape() {
		return shape;
	}
	public void setShape(PolygonShape shape) {
		this.shape = shape;
	}
	
	@Override
	public String toString() {
		return "Section [id=" + id + ", name=" + name + ", shape=" + shape + ", leveeId=" + leveeId + "]";
	}
}