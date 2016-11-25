package pl.ismop.web.client.dap.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.Json;

import pl.ismop.web.client.dap.levee.PolygonShape;
import pl.ismop.web.client.geojson.Geometry;
import pl.ismop.web.client.geojson.LineGeometry;
import pl.ismop.web.client.geojson.MapFeature;

public class Profile extends MapFeature {
	private String id;

	@Json(name = "section_id")
	private String sectionId;

	@Json(name = "profile_shape")
	private PolygonShape shape;

	@Json(name = "device_ids")
	private List<String> deviceIds;

	@Json(name = "device_aggregation_ids")
	private List<String> deviceAggregationIds;

	private List<String> vendors;

	@Json(name = "base_height")
	private double baseHeight;

	private String name;

    @Override
    public String getId() {
		return id;
	}

	@Override
	public String getFeatureType() {
		return "profile";
	}

	@Override
	public Geometry getFeatureGeometry() {
		if (getShape() != null) {
			LineGeometry lineGeometry = new LineGeometry();
			lineGeometry.setCoordinates(getShape().getCoordinates());
			return lineGeometry;
		} else {
			return null;
		}
	}

	@Override
    public boolean isAdjustBounds() {
		return true;
	}

	@Override
	public Map<String, String> getAdditionalFeatureProperties() {
		Map<String, String> properties = new HashMap<>();
		if (getVendors() != null && getVendors().size() > 0) {
			properties.put("colour_type", getVendors().get(0));
		}

		return properties;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public PolygonShape getShape() {
		return shape;
	}

	public void setShape(PolygonShape shape) {
		this.shape = shape;
	}

	public List<String> getDeviceIds() {
		return deviceIds;
	}

	public void setDeviceIds(List<String> deviceIds) {
		this.deviceIds = deviceIds;
	}

	public List<String> getDeviceAggregationIds() {
		return deviceAggregationIds;
	}

	public void setDeviceAggregationIds(List<String> deviceAggregationIds) {
		this.deviceAggregationIds = deviceAggregationIds;
	}

	public List<String> getVendors() {
		return vendors;
	}

	public void setVendors(List<String> vendors) {
		this.vendors = vendors;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Profile other = (Profile) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	public void setBaseHeight(double baseHeight) {
		this.baseHeight = baseHeight;
	}
}