package pl.ismop.web.client.dap.deviceaggregation;

import java.util.List;

import org.fusesource.restygwt.client.Json;

public class DeviceAggregationsResponse {
	@Json(name = "device_aggregations")
	private List<DeviceAggregation> deviceAggregations;

	public List<DeviceAggregation> getDeviceAggregations() {
		return deviceAggregations;
	}

	public void setDeviceAggregations(List<DeviceAggregation> deviceAggregations) {
		this.deviceAggregations = deviceAggregations;
	}
}