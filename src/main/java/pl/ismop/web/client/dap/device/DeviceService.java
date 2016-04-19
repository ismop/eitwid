package pl.ismop.web.client.dap.device;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface DeviceService extends RestService {
	@GET
	@Path("devices?device_aggregation_id={deviceAggregationIdFilter}&visible=true")
	void getDevices(@PathParam("deviceAggregationIdFilter") String deviceAggregationIdFilter,
			MethodCallback<DevicesResponse> callback);

	@GET
	@Path("devices?device_type={deviceType}&visible=true")
	void getDevicesForType(@PathParam("deviceType") String deviceType,
			MethodCallback<DevicesResponse> methodCallback);

	@GET
	@Path("devices?device_type={deviceType}&section_id={sectionId}&visible=true")
	void getDevicesForTypeAndSectionId(@PathParam("deviceType") String deviceType,
			@PathParam("sectionId") String sectionId,
			MethodCallback<DevicesResponse> methodCallback);

	@GET
	@Path("devices?section_id={sectionId}&visible=true")
	void getDevicesForSectionId(@PathParam("sectionId") String sectionId,
			MethodCallback<DevicesResponse> methodCallback);

	@GET
	@Path("devices?section_id={sectionId}&device_type={deviceType}&visible=true")
	void getDevicesForSectionIdAndType(@PathParam("sectionId") String sectionId,
			@PathParam("deviceType") String deviceType,
			MethodCallback<DevicesResponse> methodCallback);

	@GET
	@Path("devices?id={idFilter}&visible=true")
	void getDevicesForIds(@PathParam("idFilter") String idFilter,
			MethodCallback<DevicesResponse> methodCallback);

	@GET
	@Path("devices?custom_id={customIds}&visible=true")
	void getDevicesFotCustomIds(@PathParam("customIds") String customIds,
			MethodCallback<DevicesResponse> methodCallback);
	
	@GET
	@Path("devices?levee_id={leveeId}&visible=true")
	void getLeveeDevices(@PathParam("leveeId") Integer leveeId,
			MethodCallback<DevicesResponse> methodCallback);
}