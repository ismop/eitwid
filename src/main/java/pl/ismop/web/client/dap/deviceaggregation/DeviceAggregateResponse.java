package pl.ismop.web.client.dap.deviceaggregation;

import java.util.List;

import org.fusesource.restygwt.client.Json;

public class DeviceAggregationsResponse {
	@Json(name = "device_aggregations")
	private List<DeviceAggregate> deviceAggregations;

	public List<DeviceAggregate> getDeviceAggregations() {
		return deviceAggregations;
	}

	public void setDeviceAggregations(List<DeviceAggregate> deviceAggregations) {
		this.deviceAggregations = deviceAggregations;
	}
}