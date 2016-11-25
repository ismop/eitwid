package pl.ismop.web.client.dap.device;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface DeviceDirectService extends DirectRestService {
	@GET
	@Path("devices?device_aggregation_id={deviceAggregationIdFilter}&visible=true")
	DevicesResponse getDevices(
			@PathParam("deviceAggregationIdFilter") String deviceAggregationIdFilter);

	@GET
	@Path("devices?device_type={deviceType}&visible=true")
	DevicesResponse getDevicesForType(@PathParam("deviceType") String deviceType);

	@GET
	@Path("devices?device_type={deviceType}&section_id={sectionId}&visible=true")
	DevicesResponse getDevicesForTypeAndSectionId(@PathParam("deviceType") String deviceType,
			@PathParam("sectionId") String sectionId);

	@GET
	@Path("devices?section_id={sectionId}&visible=true")
	DevicesResponse getDevicesForSectionId(@PathParam("sectionId") String sectionId);

	@GET
	@Path("devices?section_id={sectionId}&device_type={deviceType}&visible=true")
	DevicesResponse getDevicesForSectionIdAndType(@PathParam("sectionId") String sectionId,
			@PathParam("deviceType") String deviceType);

	@GET
	@Path("devices?id={idFilter}&visible=true")
	DevicesResponse getDevicesForIds(@PathParam("idFilter") String idFilter);

	@GET
	@Path("devices?custom_id={customIds}&visible=true")
	DevicesResponse getDevicesFotCustomIds(@PathParam("customIds") String customIds);

	@GET
	@Path("devices?levee_id={leveeId}&visible=true")
	DevicesResponse getLeveeDevices(@PathParam("leveeId") Integer leveeId);
}
