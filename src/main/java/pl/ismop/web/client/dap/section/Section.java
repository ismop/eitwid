package pl.ismop.web.client.dap.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.fusesource.restygwt.client.Json;

import pl.ismop.web.client.dap.levee.PolygonShape;
import pl.ismop.web.client.geojson.Geometry;
import pl.ismop.web.client.geojson.MapFeature;
import pl.ismop.web.client.geojson.PolygonGeometry;

public class Section extends MapFeature {

	private String id;

	private String name;

	private PolygonShape shape;

	@Json(name = "levee_id")
	@XmlElement(name = "levee_id")
	private String leveeId;

	@Json(name = "soil_type_label")
	@XmlElement(name = "soil_type_label")
	private String soilTypeLabel;

	@Json(name = "soil_type_name")
	@XmlElement(name = "soil_type_name")
	private String soilTypeName;

	@Json(name = "bulk_density_min")
	@XmlElement(name = "bulk_density_min")
	private String bulkDensityMin;

	@Json(name = "bulk_density_max")
	@XmlElement(name = "bulk_density_max")
	private String bulkDensityMax;

	@Json(name = "bulk_density_avg")
	@XmlElement(name = "bulk_density_avg")
	private String bulkDensityAvg;

	@Json(name = "granular_density_min")
	@XmlElement(name = "granular_density_min")
	private String granularDensityMin;

	@Json(name = "granular_density_max")
	@XmlElement(name = "granular_density_max")
	private String granularDensityMax;

	@Json(name = "granular_density_avg")
	@XmlElement(name = "granular_density_avg")
	private String granularDensityAvg;

	@Json(name = "filtration_coefficient_min")
	@XmlElement(name = "filtration_coefficient_min")
	private String filtrationCoefficientMin;

	@Json(name = "filtration_coefficient_max")
	@XmlElement(name = "filtration_coefficient_max")
	private String filtrationCoefficientMax;

	@Json(name = "filtration_coefficient_avg")
	@XmlElement(name = "filtration_coefficient_avg")
	private String filtrationCoefficientAvg;


	public String getLeveeId() {
		return leveeId;
	}

	public void setLeveeId(String leveeId) {
		this.leveeId = leveeId;
	}

	@Override
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
			List<List<List<Double>>> polygonCoordinates = new ArrayList<>();
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

	@Override
	public Map<String, String> getAdditionalFeatureProperties() {
		Map<String, String> properties = new HashMap<>();
		properties.put("colour_type", getSoilTypeLabel());

		return properties;
	}

	@Override
	public boolean isAdjustBounds() {
		return true;
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

	public String getSoilTypeLabel() {
		return soilTypeLabel;
	}

	public void setSoilTypeLabel(String soilTypeLabel) {
		this.soilTypeLabel = soilTypeLabel;
	}

	public String getSoilTypeName() {
		return soilTypeName;
	}

	public void setSoilTypeName(String soilTypeName) {
		this.soilTypeName = soilTypeName;
	}

	public String getBulkDensityMin() {
		return bulkDensityMin;
	}

	public void setBulkDensityMin(String bulkDensityMin) {
		this.bulkDensityMin = bulkDensityMin;
	}

	public String getBulkDensityMax() {
		return bulkDensityMax;
	}

	public void setBulkDensityMax(String bulkDensityMax) {
		this.bulkDensityMax = bulkDensityMax;
	}

	public String getBulkDensityAvg() {
		return bulkDensityAvg;
	}

	public void setBulkDensityAvg(String bulkDensityAvg) {
		this.bulkDensityAvg = bulkDensityAvg;
	}

	public String getGranularDensityMin() {
		return granularDensityMin;
	}

	public void setGranularDensityMin(String granularDensityMin) {
		this.granularDensityMin = granularDensityMin;
	}

	public String getGranularDensityMax() {
		return granularDensityMax;
	}

	public void setGranularDensityMax(String granularDensityMax) {
		this.granularDensityMax = granularDensityMax;
	}

	public String getGranularDensityAvg() {
		return granularDensityAvg;
	}

	public void setGranularDensityAvg(String granularDensityAvg) {
		this.granularDensityAvg = granularDensityAvg;
	}

	public String getFiltrationCoefficientMin() {
		return filtrationCoefficientMin;
	}

	public void setFiltrationCoefficientMin(String filtrationCoefficientMin) {
		this.filtrationCoefficientMin = filtrationCoefficientMin;
	}

	public String getFiltrationCoefficientMax() {
		return filtrationCoefficientMax;
	}

	public void setFiltrationCoefficientMax(String filtrationCoefficientMax) {
		this.filtrationCoefficientMax = filtrationCoefficientMax;
	}

	public String getFiltrationCoefficientAvg() {
		return filtrationCoefficientAvg;
	}

	public void setFiltrationCoefficientAvg(String filtrationCoefficientAvg) {
		this.filtrationCoefficientAvg = filtrationCoefficientAvg;
	}

	@Override
	public String toString() {
		return "Section{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", shape=" + shape +
				", leveeId='" + leveeId + '\'' +
				", soilTypeLabel='" + soilTypeLabel + '\'' +
				", soilTypeName='" + soilTypeName + '\'' +
				", bulkDensityMin='" + bulkDensityMin + '\'' +
				", bulkDensityMax='" + bulkDensityMax + '\'' +
				", bulkDensityAvg='" + bulkDensityAvg + '\'' +
				", granularDensityMin='" + granularDensityMin + '\'' +
				", granularDensityMax='" + granularDensityMax + '\'' +
				", granularDensityAvg='" + granularDensityAvg + '\'' +
				", filtrationCoefficientMin='" + filtrationCoefficientMin + '\'' +
				", filtrationCoefficientMax='" + filtrationCoefficientMax + '\'' +
				", filtrationCoefficientAvg='" + filtrationCoefficientAvg + '\'' +
				'}';
	}
}
