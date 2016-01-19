package pl.ismop.web.client.dap.section;

import org.fusesource.restygwt.client.Json;
import pl.ismop.web.client.dap.levee.PolygonShape;
import pl.ismop.web.client.geojson.Geometry;
import pl.ismop.web.client.geojson.MapFeature;
import pl.ismop.web.client.geojson.PolygonGeometry;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class Section extends MapFeature {
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

	@Override
	public String getFeatureType() {
		return "section";
	}

	@Override
	public Geometry getFeatureGeometry() {
		if (isValidShape()) {
			List<List<List<Double>>> polygonCoordinates = new ArrayList<List<List<Double>>>();
			polygonCoordinates.add(getShape().getCoordinates());
			PolygonGeometry polygonGeometry = new PolygonGeometry();
			polygonGeometry.setCoordinates(polygonCoordinates);
			return polygonGeometry;
		} else {
			return null;
		}
	}

	private boolean isValidShape() {
		if (getShape() != null) {
			List<List<Double>> coordinates = getShape().getCoordinates();
			return String.valueOf(coordinates.get(0).get(0)).
					equals(String.valueOf(coordinates.get(coordinates.size() - 1).get(0)));
		} else {
			return false;
		}
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