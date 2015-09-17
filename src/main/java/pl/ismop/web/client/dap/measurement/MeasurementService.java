package pl.ismop.web.client.dap.measurement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface MeasurementService  extends RestService {
	@GET
	@Path("measurements?timeline_id={timelineIds}&time_from={timeFrom}&time_to={timeTo}")
	void getMeasurements(@PathParam("timelineIds") String timelineIds,
						 @PathParam("timeFrom") String timeFrom, @PathParam("timeTo") String timeTo,
						 MethodCallback<MeasurementsResponse> callback);

	@GET
	@Path("measurements?timeline_id={timelineIds}&time_from={timeFrom}&time_to={timeTo}&limit=last")
	void getLastMeasurements(@PathParam("timelineIds") String timelineIds,
							 @PathParam("timeFrom") String timeFrom, @PathParam("timeTo") String timeTo,
						 	 MethodCallback<MeasurementsResponse> callback);
}