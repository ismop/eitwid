package pl.ismop.web.client.dap.measurement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.Options;

import pl.ismop.web.client.dap.DapDispatcher;

@Options(dispatcher = DapDispatcher.class, serviceRootKey = "dap")
public interface MeasurementDirectService  extends DirectRestService {
	@GET
	@Path("measurements?timeline_id={timelineIds}&time_from={timeFrom}&time_to={timeTo}")
	MeasurementsResponse getMeasurements(@PathParam("timelineIds") String timelineIds,
			 @PathParam("timeFrom") String timeFrom, @PathParam("timeTo") String timeTo);

	@GET
	@Path("measurements?timeline_id={timelineIds}&time_from={timeFrom}&time_to={timeTo}&limit=last")
	MeasurementsResponse getLastMeasurements(@PathParam("timelineIds") String timelineIds,
			 @PathParam("timeFrom") String timeFrom,
			 @PathParam("timeTo") String timeTo);

	@GET
	@Path("measurements?timeline_id={timelineIds}&time_to={timeTo}&limit=last")
	MeasurementsResponse getLastMeasurementsOnlyUntil(@PathParam("timelineIds") String timelineIds,
			 @PathParam("timeTo") String timeTo);

	@GET
	@Path("measurements?timeline_id={timelineIds}&quantity={quantity}")
	MeasurementsResponse getMeasurementsWithQuantity(@PathParam("timelineIds") String timelineIds,
			@PathParam("quantity") int quantity);

	@GET
	@Path("measurements?timeline_id={timelineIds}&time_from={timeFrom}&time_to={timeTo}"
			+ "&quantity={quantity}")
	MeasurementsResponse getMeasurementsWithQuantityAndTime(
			@PathParam("timelineIds") String timelineIds,
			@PathParam("timeFrom") String timeFrom,
			@PathParam("timeTo") String timeTo,
			@PathParam("quantity") int quantity);

	@GET
	@Path("measurements?timeline_id={timelineIds}")
	MeasurementsResponse getAllMeasurements(@PathParam("timelineIds") String timelineIds);
}
