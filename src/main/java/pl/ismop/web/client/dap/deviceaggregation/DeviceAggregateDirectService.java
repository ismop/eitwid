package pl.ismop.web.client.dap.deviceaggregation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface DeviceAggregateDirectService extends DirectRestService {
	@GET
	@Path("device_aggregations?profile_id={profileIdFilter}")
	DeviceAggregateResponse getDeviceAggregates(
			@PathParam("profileIdFilter") String profileIdFilter);

	@GET
	@Path("device_aggregations?id={idFilter}")
	DeviceAggregateResponse getDeviceAggregatesForIds(@PathParam("idFilter") String idFilter);

	@GET
	@Path("device_aggregations?device_aggregation_type={type}")
	DeviceAggregateResponse getDeviceAggregatesForType(@PathParam("type") String type);

	@GET
	@Path("device_aggregations?device_aggregation_type={type}&levee_id={levee_id}")
	DeviceAggregateResponse getDeviceAggregatesForType(@PathParam("type") String type,
			@PathParam("levee_id") String leveeId);

	@GET
	@Path("device_aggregations?section_id={sectionIdFilter}")
	DeviceAggregateResponse getDeviceAggregatesForSectionIds(
			@PathParam("sectionIdFilter") String sectionIdFilter);
}
