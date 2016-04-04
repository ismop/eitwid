package pl.ismop.web.client.geojson;

import java.util.HashMap;
import java.util.Map;

public abstract class MapFeature {
    public abstract String getId();
    public abstract String getFeatureType();
    public abstract Geometry getFeatureGeometry();

    public String getFeatureId() {
        return getFeatureType() + "-" + getId();
    }

    public boolean isAdjustBounds() {
        return false;
    }

    public Map<String, String> getAdditionalFeatureProperties() {
        return new HashMap<>();
    }
}
