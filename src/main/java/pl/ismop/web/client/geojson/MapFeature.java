package pl.ismop.web.client.geojson;

public abstract class MapFeature {
    public abstract String getId();
    public abstract String getFeatureType();
    public abstract Geometry getFeatureGeometry();

    public String getFeatureId() {
        return getFeatureType() + "-" + getId();
    }
}
