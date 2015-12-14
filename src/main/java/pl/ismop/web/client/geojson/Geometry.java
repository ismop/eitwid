package pl.ismop.web.client.geojson;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = LineGeometry.class, name = "LineString"),
	@JsonSubTypes.Type(value = PointGeometry.class, name = "Point"),
	@JsonSubTypes.Type(value = PolygonGeometry.class, name = "Polygon"),
})
public abstract class Geometry {
	private String type;
	
	public Geometry(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}